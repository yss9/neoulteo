from contextlib import asynccontextmanager
from pathlib import Path
import io
import os
import shutil

import chromadb
import numpy as np
import pypdf
from fastapi import FastAPI, File, Query, Request, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import RedirectResponse
from openai import OpenAI
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer


GMS_OPENAI_API_KEY = os.getenv("GMS_OPENAI_API_KEY", "")
GMS_OPENAI_BASE_URL = os.getenv("GMS_OPENAI_BASE_URL", "https://gms.ssafy.io/gmsapi/api.openai.com/v1")
GPT_MODEL = os.getenv("GMS_OPENAI_MODEL", "gpt-4o-mini")

DATA_DIR = Path("./data")
DATA_DIR.mkdir(parents=True, exist_ok=True)

RAG_MODEL_NAME = "paraphrase-multilingual-MiniLM-L12-v2"
CHUNK_SIZE = 1000
OVERLAP = 100
SIMILARITY_THRESHOLD = 0.20
CHROMA_DB_PATH = "./chroma_data"

DB = []
DB_EMBEDDINGS = None
rag_model = None
openai_client = None
collection = None


class ChatReq(BaseModel):
    message: str


def extract_text(file_path=None, content=None, file_ext=None):
    def read_pdf(reader: pypdf.PdfReader) -> str:
        texts = []
        for page in reader.pages:
            text = page.extract_text()
            if text:
                texts.append(text)
        return "\n".join(texts)

    if file_path:
        ext = file_path.suffix.lower()
        if ext == ".pdf":
            return read_pdf(pypdf.PdfReader(file_path))

        raw_bytes = file_path.read_bytes()
        try:
            return raw_bytes.decode("utf-8")
        except UnicodeDecodeError:
            return raw_bytes.decode("cp949", errors="ignore")

    if content is not None and file_ext:
        if file_ext == ".pdf":
            return read_pdf(pypdf.PdfReader(io.BytesIO(content)))

        try:
            return content.decode("utf-8")
        except UnicodeDecodeError:
            return content.decode("cp949", errors="ignore")

    return ""


def gms_not_configured_response():
    return {
        "success": False,
        "answer": "GMS_OPENAI_API_KEY가 설정되지 않았습니다.",
        "message": "GMS_OPENAI_API_KEY를 설정한 뒤 FastAPI 서버를 다시 실행하세요.",
    }


def has_gms_key():
    return bool(GMS_OPENAI_API_KEY.strip())


def chunk_text(text, size=CHUNK_SIZE, overlap=OVERLAP):
    lines = [line.strip() for line in text.split("\n") if line.strip()]

    # 관광지 덤프처럼 한 줄이 한 항목인 문서는 줄 단위로 쪼개는 편이 검색 품질이 낫다.
    if lines and (sum(len(line) for line in lines[: min(5, len(lines))]) / min(5, len(lines))) < 150:
        return lines

    if overlap >= size:
        raise ValueError("Overlap must be less than chunk size")

    chunks = []
    start = 0
    while start < len(text):
        end = start + size
        chunks.append(text[start:end])
        start += size - overlap
    return chunks


def normalize(vecs):
    arr = np.asarray(vecs, dtype=np.float32)
    if arr.ndim == 1:
        denom = np.linalg.norm(arr) + 1e-12
        return arr / denom
    denom = np.linalg.norm(arr, axis=1, keepdims=True) + 1e-12
    return arr / denom


def add_to_db(chunks, source):
    global DB, DB_EMBEDDINGS

    start_id = len(DB)
    new_items = [{"id": str(start_id + i), "text": chunk, "source": source} for i, chunk in enumerate(chunks)]
    DB.extend(new_items)

    new_embeddings = rag_model.encode([item["text"] for item in new_items])
    new_embeddings = normalize(new_embeddings)

    if DB_EMBEDDINGS is None or len(DB_EMBEDDINGS) == 0:
        DB_EMBEDDINGS = new_embeddings
    else:
        DB_EMBEDDINGS = np.vstack([DB_EMBEDDINGS, new_embeddings])

    collection.add(
        documents=[item["text"] for item in new_items],
        embeddings=new_embeddings.tolist(),
        metadatas=[{"source": item["source"]} for item in new_items],
        ids=[item["id"] for item in new_items],
    )
    return len(new_items)


def reset_chroma_db():
    global DB, DB_EMBEDDINGS

    DB = []
    DB_EMBEDDINGS = None
    chroma_path = Path(CHROMA_DB_PATH)
    if chroma_path.exists():
        shutil.rmtree(chroma_path)
    chroma_path.mkdir(parents=True, exist_ok=True)


@asynccontextmanager
async def lifespan(app: FastAPI):
    global rag_model, openai_client, collection

    print("1. AI 모델과 GMS 클라이언트를 초기화합니다.")
    openai_client = OpenAI(api_key=GMS_OPENAI_API_KEY or "missing-gms-key", base_url=GMS_OPENAI_BASE_URL)
    rag_model = SentenceTransformer(RAG_MODEL_NAME)

    reset_chroma_db()
    chroma_client = chromadb.PersistentClient(path=CHROMA_DB_PATH)
    collection = chroma_client.create_collection("ssafy_docs", metadata={"hnsw:space": "cosine"})

    print(f"2. 문서를 읽는 중입니다. 경로: {DATA_DIR}")
    for file_path in DATA_DIR.glob("*"):
        if not file_path.is_file():
            continue
        try:
            text = extract_text(file_path=file_path)
            if not text.strip():
                continue
            chunks = chunk_text(text)
            add_to_db(chunks, file_path.name)
            print(f"   - 로드 완료: {file_path.name}")
        except Exception as exc:
            print(f"   - 로드 실패: {file_path.name}: {exc}")

    print(f"3. 벡터 생성 완료: 총 {len(DB)}개 청크")
    print("FastAPI AI 서버가 시작되었습니다.")
    yield
    print("FastAPI AI 서버가 종료됩니다.")


app = FastAPI(lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.post("/chat")
def chat(req: ChatReq):
    if not has_gms_key():
        return gms_not_configured_response()

    response = openai_client.chat.completions.create(
        model=GPT_MODEL,
        messages=[
            {
                "role": "system",
                "content": "당신은 Neoulteo 여행 서비스의 친절한 AI 도우미입니다. 한국어로 간결하고 실용적으로 답하세요.",
            },
            {"role": "user", "content": req.message},
        ],
    )
    return {"success": True, "answer": response.choices[0].message.content}


@app.post("/upload")
async def upload_file(request: Request, file: UploadFile = File(...)):
    allowed_extensions = {".txt", ".md", ".pdf"}
    file_ext = Path(file.filename).suffix.lower()

    if file_ext not in allowed_extensions:
        return {"success": False, "message": "지원하지 않는 파일 형식입니다. txt, md, pdf만 업로드할 수 있습니다."}

    try:
        content = await file.read()
        text = extract_text(content=content, file_ext=file_ext)

        if not text.strip():
            return {"success": False, "message": "파일에서 읽을 수 있는 텍스트가 없습니다."}

        chunks = chunk_text(text)
        chunks_added = add_to_db(chunks, file.filename)

        save_path = DATA_DIR / file.filename
        with open(save_path, "wb") as out:
            out.write(content)

        result = {
            "success": True,
            "message": f"{file.filename} 업로드 완료. {chunks_added}개 청크가 추가되었습니다.",
            "chunks_added": chunks_added,
        }

        referer = request.headers.get("referer")
        x_requested_with = request.headers.get("X-Requested-With")
        if x_requested_with == "XMLHttpRequest" or not referer:
            return result
        return RedirectResponse(url=referer, status_code=303)

    except Exception as exc:
        return {"success": False, "message": f"파일 처리 중 오류가 발생했습니다: {exc}"}


@app.post("/search")
def search(req: ChatReq, top_k: int = Query(5, ge=1, le=20), debug: bool = Query(True)):
    if DB_EMBEDDINGS is None or len(DB) == 0:
        return {
            "answer": "검색할 문서가 없습니다. ai-server/data 폴더에 문서를 넣거나 /upload로 문서를 업로드하세요.",
            "score": None,
            "source": None,
        }

    query_vec = normalize(rag_model.encode(req.message))
    scores = np.dot(DB_EMBEDDINGS, query_vec)

    idxs = np.argsort(scores)[::-1][:top_k]
    candidates = [
        {
            "rank": rank + 1,
            "score": float(scores[index]),
            "source": DB[index]["source"],
            "full_text": DB[index]["text"],
        }
        for rank, index in enumerate(idxs)
    ]

    best = candidates[0]
    is_valid = best["score"] >= SIMILARITY_THRESHOLD

    return {
        "answer": best["full_text"] if is_valid else "관련된 내용을 찾을 수 없습니다.",
        "score": best["score"],
        "source": best["source"] if is_valid else None,
        "candidates": candidates if debug else None,
    }


@app.post("/integrated-chat")
def integrated_chat(req: ChatReq, n_results: int = Query(10, ge=1, le=20), debug: bool = Query(False)):
    if not has_gms_key():
        return gms_not_configured_response()

    query_embedding = normalize(rag_model.encode([req.message]))
    results = collection.query(query_embeddings=query_embedding.tolist(), n_results=n_results)

    docs = results.get("documents", [[]])[0]
    metas = results.get("metadatas", [[]])[0]
    dists = results.get("distances", [[]])[0]

    candidates = []
    for doc, meta, dist in zip(docs, metas, dists):
        similarity = 1.0 - float(dist)
        candidates.append(
            {
                "source": meta.get("source"),
                "similarity": similarity,
                "text": doc,
            }
        )

    picked = [candidate for candidate in candidates if candidate["similarity"] >= SIMILARITY_THRESHOLD]

    if picked:
        picked = sorted(picked, key=lambda item: item["similarity"], reverse=True)[:n_results]
        context = "\n\n".join(
            [f"[출처: {item['source']} | 유사도: {item['similarity']:.2f}]\n{item['text']}" for item in picked]
        )
        source_tag = ", ".join(sorted({item["source"] for item in picked}))
    else:
        context = "(검색 결과 없음)"
        source_tag = "GMS 일반 답변"

    prompt = f"""
당신은 Neoulteo의 친절하고 전문적인 여행 가이드 AI 챗봇입니다.
사용자의 질문에 답할 때 아래 [참고 문서]에 포함된 정보를 우선적으로 사용하세요.

답변 규칙:
1. 질문과 관련된 관광지, 주소, 전화번호, 특징이 참고 문서에 있으면 그대로 활용하세요.
2. 참고 문서에 없는 내용은 지어내지 말고, 문서에서 확인되지 않는다고 말하세요.
3. 참고 문서가 "(검색 결과 없음)"이면 일반적인 여행 조언만 간단히 제공하세요.
4. 답변은 한국어로 자연스럽고 실용적으로 작성하세요.

[참고 문서]
{context}
""".strip()

    response = openai_client.chat.completions.create(
        model=GPT_MODEL,
        messages=[
            {"role": "system", "content": prompt},
            {"role": "user", "content": req.message},
        ],
    )

    payload = {"success": True, "answer": response.choices[0].message.content, "source": source_tag}
    if debug:
        payload["candidates"] = [
            {"source": item["source"], "similarity": round(item["similarity"], 4)} for item in candidates
        ]
        payload["picked"] = [{"source": item["source"], "similarity": round(item["similarity"], 4)} for item in picked]
    return payload


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)
