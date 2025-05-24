import axios from 'axios';

// Vite 환경 변수에서 API 기본 URL 가져오는 부분임.
// .env.development 또는 .env.production 파일에 정의된 VITE_APP_API_BASE_URL 값을 사용함.
// VITE_APP_API_BASE_URL=http://localhost:8081 처럼 .env 파일에 써둔 값을 가져옴.
const API_BASE_URL = import.meta.env.VITE_APP_API_BASE_URL || 'http://localhost:8081'; // .env 파일에 값이 없으면 기본값으로 'http://localhost:8081'을 사용함.

// Axios 인스턴스 생성하는 부분임.
// 이 인스턴스에 우리가 원하는 기본 설정들을 넣어둘 것임.
const apiClient = axios.create({
  baseURL: `${API_BASE_URL}/api/v1`, // API 요청 시 이 주소를 기본으로 사용할 것임.
  timeout: 10000, // 서버 응답을 10초(10000밀리초)까지 기다릴 것임.
  headers: { // 모든 요청에 기본적으로 포함될 정보들임.
    'Content-Type': 'application/json', // 보내는 데이터는 JSON 형식이라고 서버에 알려주는 것임.
    // 'X-Requested-With': 'XMLHttpRequest', // 필요에 따라 추가할 수 있는 정보임 (지금은 몰라도 됨).
  },
  withCredentials: true, // 요청 보낼 때 쿠키 같은 사용자 자격 증명 정보를 포함할지 설정하는 것임.
                         // 백엔드 CORS 설정의 allowCredentials: true 와 짝꿍임.
});

// --- 요청 인터셉터 (Request Interceptor) ---
// 모든 API 요청이 서버로 보내지기 전에 중간에 가로채서 뭔가를 할 수 있는 부분임.
apiClient.interceptors.request.use(
  (config) => { // 요청 설정(config) 정보를 받아서
    // 요청 보내기 전에 할 작업들을 여기에 작성함 (예: 로그인 토큰 헤더에 추가).
    const token = localStorage.getItem('token'); // 로컬 스토리지에서 JWT 토큰 가져오기
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`; // 헤더에 토큰 추가하는 것
    }
    console.log('Request Interceptor:', config); // 개발 중에 어떤 요청이 나가는지 확인용 로그임.
    return config; // 설정을 변경했으면 변경된 설정을 다시 반환해야 요청이 계속 진행됨.
  },
  (error) => { // 요청 준비 중에 에러가 나면 여기로 옴.
    // 요청 에러 처리 로직을 여기에 작성함.
    console.error('Request Interceptor Error:', error);
    return Promise.reject(error); // 에러를 다음 단계로 넘겨주는 것임.
  }
);

// --- 응답 인터셉터 (Response Interceptor) ---
// 서버로부터 응답을 받은 후에 중간에 가로채서 뭔가를 할 수 있는 부분임.
apiClient.interceptors.response.use(
  (response) => { // 서버 응답(response) 정보를 받아서
    // 응답 데이터를 가공하거나 공통적으로 할 작업들을 여기에 작성함.
    console.log('Response Interceptor:', response); // 개발 중에 어떤 응답이 오는지 확인용 로그임.
    return response; // 처리한 응답 정보를 다시 반환해야 다음 단계로 넘어감 (보통 response.data 만 반환하기도 함).
  },
  (error) => { // 서버 응답 중에 에러가 나면 여기로 옴.
    // 응답 에러 처리 로직을 여기에 작성함.
    console.error('Response Interceptor Error:', error);

    // if (error.response) { // 서버가 에러 응답을 보낸 경우 (예: 404, 500 에러)
    //   console.error('Status:', error.response.status); // 에러 상태 코드 확인 예시임.
    //   console.error('Data:', error.response.data); // 서버가 에러와 함께 보낸 데이터 확인 예시임.
    //   // 401 (인증 안됨) 에러 발생 시 로그인 페이지로 이동시키는 등의 처리를 여기에 할 수 있음.
    //   // if (error.response.status === 401) { alert('다시 로그인해주세요.'); /* router.push('/login'); */ }
    // } else if (error.request) { // 요청은 보냈는데 서버 응답을 전혀 못 받은 경우 (네트워크 끊김 등)
    //   console.error('Request Error:', error.request); // 요청 자체에 대한 정보 확인 예시임.
    //   // alert('서버에 연결할 수 없습니다. 네트워크 상태를 확인해주세요.'); // 사용자에게 알림 메시지 보여주기 예시임.
    // } else { // 요청 설정 과정 등에서 에러가 발생한 경우
    //   console.error('Error Message:', error.message); // 에러 메시지 확인 예시임.
    // }

    return Promise.reject(error); // 에러를 계속 넘겨주어 이 코드를 사용한 곳에서 에러를 처리할 수 있게 함.
  }
);

export default apiClient; // 우리가 설정한 Axios 인스턴스를 다른 파일에서 가져다 쓸 수 있게 내보내는 것임.