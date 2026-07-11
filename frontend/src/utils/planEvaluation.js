const CATEGORY_LABELS = {
  12: "관광지",
  14: "문화시설",
  15: "축제/공연",
  25: "여행코스",
  28: "레포츠",
  32: "숙박",
  38: "쇼핑",
  39: "음식점"
};

function getDayNo(place) {
  const dayNo = Number(place?.dayNo || 1);
  return Number.isFinite(dayNo) && dayNo > 0 ? dayNo : 1;
}

function getCoordinate(place) {
  const lat = Number(place?.mapy ?? place?.latitude);
  const lng = Number(place?.mapx ?? place?.longitude);
  if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
    return null;
  }
  return { lat, lng };
}

function distanceKm(from, to) {
  const earthRadiusKm = 6371;
  const toRad = (value) => (value * Math.PI) / 180;
  const dLat = toRad(to.lat - from.lat);
  const dLng = toRad(to.lng - from.lng);
  const lat1 = toRad(from.lat);
  const lat2 = toRad(to.lat);
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLng / 2) ** 2;
  return earthRadiusKm * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

function categoryLabel(place) {
  return place?.contentTypeName || CATEGORY_LABELS[Number(place?.contentTypeId ?? place?.contenttypeid)] || "기타";
}

function groupByDay(places) {
  return (places || []).reduce((days, place) => {
    const dayNo = getDayNo(place);
    if (!days.has(dayNo)) {
      days.set(dayNo, []);
    }
    days.get(dayNo).push(place);
    return days;
  }, new Map());
}

function dayDistance(places) {
  let total = 0;
  let maxLeg = 0;
  let measurableLegs = 0;

  for (let index = 1; index < places.length; index += 1) {
    const from = getCoordinate(places[index - 1]);
    const to = getCoordinate(places[index]);
    if (!from || !to) continue;
    const leg = distanceKm(from, to);
    total += leg;
    maxLeg = Math.max(maxLeg, leg);
    measurableLegs += 1;
  }

  return { total, maxLeg, measurableLegs };
}

function nearestNeighborSort(places) {
  const remaining = [...places];
  const sorted = [];
  let current = remaining.shift();

  while (current) {
    sorted.push(current);
    const currentCoordinate = getCoordinate(current);
    if (!currentCoordinate || remaining.length === 0) {
      current = remaining.shift();
      continue;
    }

    let nextIndex = 0;
    let nextDistance = Number.POSITIVE_INFINITY;
    remaining.forEach((candidate, index) => {
      const coordinate = getCoordinate(candidate);
      if (!coordinate) return;
      const distance = distanceKm(currentCoordinate, coordinate);
      if (distance < nextDistance) {
        nextDistance = distance;
        nextIndex = index;
      }
    });
    current = remaining.splice(nextIndex, 1)[0];
  }

  return sorted;
}

export function buildOptimizedTravelPlan(places) {
  const days = groupByDay(places);
  return [...days.entries()]
    .sort(([left], [right]) => left - right)
    .flatMap(([, dayPlaces]) => nearestNeighborSort(dayPlaces));
}

export function evaluateTravelPlan(places) {
  const normalizedPlaces = places || [];
  const days = groupByDay(normalizedPlaces);
  const feedback = [];
  const daySummaries = [];
  const categoryCounts = new Map();
  let score = 100;
  let totalDistance = 0;
  let missingCoordinateCount = 0;

  if (normalizedPlaces.length === 0) {
    return {
      score: 0,
      level: "empty",
      summary: "평가할 여행 코스가 없습니다.",
      feedback: ["관광지를 먼저 추가한 뒤 코스 평가를 실행해보세요."],
      daySummaries: [],
      categorySummary: []
    };
  }

  normalizedPlaces.forEach((place) => {
    const label = categoryLabel(place);
    categoryCounts.set(label, (categoryCounts.get(label) || 0) + 1);
    if (!getCoordinate(place)) {
      missingCoordinateCount += 1;
    }
  });

  [...days.entries()]
    .sort(([left], [right]) => left - right)
    .forEach(([dayNo, dayPlaces]) => {
      const distance = dayDistance(dayPlaces);
      totalDistance += distance.total;
      daySummaries.push({
        dayNo,
        placeCount: dayPlaces.length,
        distanceKm: distance.total,
        maxLegKm: distance.maxLeg
      });

      if (dayPlaces.length >= 6) {
        score -= 16;
        feedback.push(`${dayNo}일차 관광지가 ${dayPlaces.length}곳입니다. 하루 일정이 빡빡할 수 있습니다.`);
      } else if (dayPlaces.length === 5) {
        score -= 8;
        feedback.push(`${dayNo}일차는 5곳 방문 예정입니다. 식사와 이동 시간을 넉넉히 두는 편이 좋습니다.`);
      }

      if (distance.total >= 90) {
        score -= 18;
        feedback.push(`${dayNo}일차 이동 거리가 약 ${distance.total.toFixed(1)}km로 길어 보입니다.`);
      } else if (distance.total >= 50) {
        score -= 10;
        feedback.push(`${dayNo}일차 이동 거리가 약 ${distance.total.toFixed(1)}km입니다. 가까운 장소끼리 묶어보세요.`);
      }

      if (distance.maxLeg >= 35) {
        score -= 10;
        feedback.push(`${dayNo}일차에 한 번에 ${distance.maxLeg.toFixed(1)}km 이동하는 구간이 있습니다.`);
      }
    });

  const categorySummary = [...categoryCounts.entries()]
    .map(([label, count]) => ({ label, count, ratio: count / normalizedPlaces.length }))
    .sort((left, right) => right.count - left.count);
  const dominantCategory = categorySummary[0];

  if (dominantCategory?.ratio >= 0.75 && normalizedPlaces.length >= 4) {
    score -= 10;
    feedback.push(`${dominantCategory.label} 비중이 높습니다. 음식점, 문화시설, 쇼핑 코스를 섞으면 균형이 좋아집니다.`);
  }

  if (missingCoordinateCount > 0) {
    score -= Math.min(12, missingCoordinateCount * 3);
    feedback.push(`좌표가 없는 장소 ${missingCoordinateCount}곳은 지도 동선 평가에서 제외했습니다.`);
  }

  if (feedback.length === 0) {
    feedback.push("일차별 장소 수와 이동 거리가 무난합니다. 현재 코스 그대로 진행해도 좋아 보입니다.");
  }

  const normalizedScore = Math.max(0, Math.min(100, Math.round(score)));
  const level = normalizedScore >= 82 ? "good" : normalizedScore >= 62 ? "check" : "tight";

  return {
    score: normalizedScore,
    level,
    summary:
      level === "good"
        ? "여유 있는 여행 코스입니다."
        : level === "check"
          ? "조금 조정하면 더 좋아질 코스입니다."
          : "일정이 빡빡하거나 동선 조정이 필요합니다.",
    feedback,
    daySummaries,
    categorySummary,
    totalDistanceKm: totalDistance
  };
}
