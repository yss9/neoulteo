import southKoreaMap from "@svg-maps/south-korea";

const REGION_META_BY_ID = {
  seoul: {
    code: "SEOUL",
    name: "서울",
    displayName: "서울특별시"
  },
  busan: {
    code: "BUSAN",
    name: "부산",
    displayName: "부산광역시"
  },
  daegu: {
    code: "DAEGU",
    name: "대구",
    displayName: "대구광역시"
  },
  incheon: {
    code: "INCHEON",
    name: "인천",
    displayName: "인천광역시"
  },
  gwangju: {
    code: "GWANGJU",
    name: "광주",
    displayName: "광주광역시"
  },
  daejeon: {
    code: "DAEJEON",
    name: "대전",
    displayName: "대전광역시"
  },
  ulsan: {
    code: "ULSAN",
    name: "울산",
    displayName: "울산광역시"
  },
  sejong: {
    code: "SEJONG",
    name: "세종",
    displayName: "세종특별자치시"
  },
  gyeonggi: {
    code: "GYEONGGI",
    name: "경기",
    displayName: "경기도"
  },
  gangwon: {
    code: "GANGWON",
    name: "강원",
    displayName: "강원특별자치도"
  },
  "north-chungcheong": {
    code: "CHUNGBUK",
    name: "충북",
    displayName: "충청북도"
  },
  "south-chungcheong": {
    code: "CHUNGNAM",
    name: "충남",
    displayName: "충청남도"
  },
  "north-jeolla": {
    code: "JEONBUK",
    name: "전북",
    displayName: "전북특별자치도"
  },
  "south-jeolla": {
    code: "JEONNAM",
    name: "전남",
    displayName: "전라남도"
  },
  "north-gyeongsang": {
    code: "GYEONGBUK",
    name: "경북",
    displayName: "경상북도"
  },
  "south-gyeongsang": {
    code: "GYEONGNAM",
    name: "경남",
    displayName: "경상남도"
  },
  jeju: {
    code: "JEJU",
    name: "제주",
    displayName: "제주특별자치도"
  }
};

export const KOREA_MAP_VIEW_BOX = southKoreaMap.viewBox;

export const KOREA_SIDO_PATHS = southKoreaMap.locations.map((location) => {
  const meta = REGION_META_BY_ID[location.id] ?? {
    code: location.id.toUpperCase(),
    name: location.name,
    displayName: location.name
  };

  return {
    ...meta,
    id: location.id,
    sourceName: location.name,
    d: location.path
  };
});
