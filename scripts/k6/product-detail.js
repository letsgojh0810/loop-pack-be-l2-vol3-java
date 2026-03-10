/**
 * 상품 상세 조회 부하테스트
 * - 캐시 미스(첫 요청) → 캐시 히트(이후 요청) 비교
 *
 * 실행: k6 run --out influxdb=http://localhost:8086/k6 scripts/k6/product-detail.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const BASE_URL = 'http://localhost:8080';

const errorRate = new Rate('errors');

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '1m', target: 100 },
    { duration: '2m', target: 100 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<300'],  // 캐시 히트 시 300ms 이하 기대
    errors: ['rate<0.01'],
  },
};

// 자주 조회될 상품 ID (캐시 히트 유도) vs 랜덤 ID
const HOT_PRODUCT_IDS = [1, 2, 3, 4, 5, 10, 100, 1000]; // 자주 hit될 ID

export default function () {
  // 70% 확률로 hot product, 30% 랜덤
  let productId;
  if (Math.random() < 0.7) {
    productId = HOT_PRODUCT_IDS[Math.floor(Math.random() * HOT_PRODUCT_IDS.length)];
  } else {
    productId = Math.floor(Math.random() * 100000) + 1;
  }

  const res = http.get(`${BASE_URL}/api/v1/products/${productId}`);

  const success = check(res, {
    'status 200 or 404': (r) => r.status === 200 || r.status === 404,
    'not 500': (r) => r.status !== 500,
  });

  errorRate.add(!success);
  sleep(0.3);
}

export function handleSummary(data) {
  return {
    'scripts/k6/results/product-detail-summary.json': JSON.stringify(data, null, 2),
  };
}
