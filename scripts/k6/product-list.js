/**
 * 상품 목록 조회 부하테스트
 * - 캐시 미스(첫 요청) → 캐시 히트(이후 요청) 비교
 *
 * 실행: k6 run --out influxdb=http://localhost:8086/k6 scripts/k6/product-list.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const BASE_URL = 'http://localhost:8080';

// 커스텀 메트릭
const errorRate = new Rate('errors');
const cacheHitTrend = new Trend('cache_hit_duration', true);
const cacheMissTrend = new Trend('cache_miss_duration', true);

export const options = {
  stages: [
    { duration: '30s', target: 10 },   // 워밍업
    { duration: '1m', target: 50 },    // 부하 증가
    { duration: '2m', target: 50 },    // 부하 유지
    { duration: '30s', target: 0 },    // 쿨다운
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],  // 95%는 500ms 이하
    errors: ['rate<0.01'],             // 에러율 1% 이하
  },
};

const sortOptions = ['latest', 'price_asc', 'likes_desc'];
const brandIds = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];

export default function () {
  const sort = sortOptions[Math.floor(Math.random() * sortOptions.length)];
  const brandId = Math.random() > 0.5 ? brandIds[Math.floor(Math.random() * brandIds.length)] : null;
  const page = Math.floor(Math.random() * 50);

  let url = `${BASE_URL}/api/v1/products?sort=${sort}&page=${page}&size=20`;
  if (brandId) {
    url += `&brandId=${brandId}`;
  }

  const res = http.get(url);

  const success = check(res, {
    'status 200': (r) => r.status === 200,
    'has data': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.data !== undefined;
      } catch {
        return false;
      }
    },
  });

  errorRate.add(!success);

  // X-Cache 헤더로 캐시 히트 여부 구분 (없으면 duration으로만 구분)
  const isCacheHit = res.headers['X-Cache'] === 'HIT';
  if (isCacheHit) {
    cacheHitTrend.add(res.timings.duration);
  } else {
    cacheMissTrend.add(res.timings.duration);
  }

  sleep(0.5);
}

export function handleSummary(data) {
  return {
    'scripts/k6/results/product-list-summary.json': JSON.stringify(data, null, 2),
  };
}
