import http from 'k6/http';
import { check } from 'k6';
import { Trend } from 'k6/metrics';

const listDuration = new Trend('policy_list_duration', true);
const detailDuration = new Trend('policy_detail_duration', true);
const baseUrl = __ENV.BASE_URL;
const policyId = __ENV.POLICY_ID || '00000000-0000-0000-0000-000000000001';

if (!baseUrl) {
  throw new Error('Set BASE_URL to the explicitly selected test service before running this scenario.');
}

export const options = {
  scenarios: {
    policy_read_load: {
      executor: 'constant-vus',
      vus: 50,
      duration: '2m',
    },
  },
  thresholds: {
    policy_list_duration: ['p(95)<300'],
    policy_detail_duration: ['p(95)<300'],
  },
};

export default function () {
  const listResponse = http.get(`${baseUrl}/api/v1/policies?page=0&size=20`);
  listDuration.add(listResponse.timings.duration);
  check(listResponse, {
    'policy list returns 200': (response) => response.status === 200,
  });

  const detailResponse = http.get(`${baseUrl}/api/v1/policies/${policyId}`);
  detailDuration.add(detailResponse.timings.duration);
  check(detailResponse, {
    'policy detail returns 200': (response) => response.status === 200,
  });
}
