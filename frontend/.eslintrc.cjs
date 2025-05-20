// 이 파일은 우리 프로젝트 프론트엔드 코드의 '코딩 규칙(ESLint 설정)'을 정하는 곳임.

// Node.js 에서 이 파일을 읽어 들일 수 있도록 설정을 외부로 내보내는 표준 방식임.
module.exports = {
  // 이 설정 파일이 프로젝트의 '가장 위(루트)'에 있는 설정이라는 것을 알려줌.
  // 모노레포 구조에서 각 하위 폴더마다 ESLint 설정을 따로 가질 때 유용함.
  root: true,

  // 코드가 실행될 '환경'을 정해서, 해당 환경에서 미리 정의된 전역 변수 등을 ESLint가 인식하게 함.
  env: {
    // 우리 코드가 Node.js 환경에서도 돌아갈 수 있다는 것을 알려줌 (예: 빌드 스크립트 등).
    node: true,
    // Vue 3의 <script setup> 이라는 새로운 코딩 스타일에서 사용되는 특별한 함수들(defineProps, defineEmits 등)을
    // ESLint가 에러 없이 제대로 인식하게 해주는 설정임.
    'vue/setup-compiler-macros': true,
  },

  // 다른 사람들이 미리 잘 만들어둔 '코딩 규칙 모음(설정 묶음)'들을 가져와서 사용하겠다는 목록임.
  // 목록에 있는 순서대로 규칙이 적용되며, 나중에 나오는 규칙이 앞의 규칙을 덮어쓸 수 있음.
  extends: [
    // Vue.js 3 버전 코드에 대한 '필수 권장 규칙' 모음을 가져옴.
    // 더 엄격한 'strongly-recommended'나 'recommended' 규칙으로 바꿀 수도 있음.
    'plugin:vue/vue3-essential',
    // 만약 우리 프로젝트에서 JavaScript와 함께 TypeScript를 사용한다면,
    // Vue 코드 안에서 TypeScript 문법에 대한 권장 규칙을 적용함.
    // JavaScript만 쓴다면 이 줄을 제거하거나 주석 처리해야 함.
    '@vue/typescript/recommended',
    // ESLint 자체에서 제공하는 일반적인 '필수 권장 JavaScript 규칙' 모음을 가져옴.
    'eslint:recommended',
    // Ionic Framework 프로젝트에 특화된 '권장 코딩 규칙' 모음을 가져옴.
    // Ionic 컴포넌트 사용 시 발생할 수 있는 잠재적 오류를 잡아줌.
    '@ionic/eslint-config/recommended',
    // 이 규칙 모음은 ESLint의 코드 '포맷팅(들여쓰기, 공백 등)' 관련 규칙들을 모두 '꺼버림'.
    // 이렇게 하는 이유는 코드 포맷팅은 Prettier 라는 다른 도구에게 완전히 맡기기 위함임.
    // ESLint와 Prettier의 포맷팅 규칙이 서로 다르면 충돌이 나서 코드가 왔다 갔다 할 수 있기 때문에,
    // 보통 ESLint는 '코드 문법이나 잠재적 오류'만 검사하게 하고, 포맷팅은 Prettier만 사용함.
    // 그래서 'prettier'는 extends 목록의 가장 마지막에 오는 것이 일반적임.
    'prettier',
  ],

  // ESLint가 코드를 '분석(파싱)'할 때 사용할 옵션들을 설정하는 곳임.
  parserOptions: {
    // 코드를 분석할 때 ECMAScript (JavaScript)의 2020년도 문법까지 이해하도록 설정함.
    // 최신 문법을 사용하려면 그에 맞는 연도를 설정해야 함.
    ecmaVersion: 2020,
    // parser: '@typescript-eslint/parser', // 만약 TypeScript 코드를 사용한다면, TypeScript 문법을 이해하기 위한 별도의 파서(분석기)를 지정해야 함. 지금은 주석 처리되어 있음.
  },

  // extends로 가져온 '규칙 모음'들 외에, 우리 프로젝트에서 '개별적으로' 설정하거나
  // '수정(덮어쓰기)'하고 싶은 규칙들을 정의하는 곳임.
  // 각 규칙은 '규칙 이름': '설정 값' 형태로 지정함.
  // 설정 값은 보통 'off' (규칙 끔), 'warn' (경고만), 'error' (에러 발생) 중 하나임.
  rules: {
    // console.log() 같은 코드를 사용할지 말지에 대한 규칙임.
    // process.env.NODE_ENV 는 현재 프로그램이 '어떤 환경'에서 실행되는지 알려주는 값임.
    // === 'production' ? 'warn' : 'off' -> 만약 'production'(실제 서비스 운영) 환경이면 'warn'(경고)를 띄우고,
    //                                      그렇지 않으면 ('development' 개발 환경 등) 'off'(규칙 끔) 해서 자유롭게 console.log를 쓰게 함.
    'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',

    // debugger; 같은 코드를 사용할지 말지에 대한 규칙임. (코드 실행을 멈추고 디버깅하는 코드)
    // console.log() 와 비슷하게, 운영 환경에서는 경고, 개발 환경에서는 허용하도록 설정함.
    'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off',

    // Vue의 'slot' 이라는 기능의 특정 사용 방식(deprecated)에 대한 규칙임.
    // Ionic 컴포넌트들이 이 방식을 사용할 때가 있어서 충돌 가능성이 있음.
    // 충돌을 피하기 위해 이 규칙을 'off' (끔) 함.
    'vue/no-deprecated-slot-attribute': 'off',

    // TypeScript에서 'any' 라는 타입을 명시적으로 사용하는 것에 대한 규칙임.
    // any 타입은 '어떤 종류의 데이터든 될 수 있다'는 뜻이라 TypeScript의 장점(타입 안정성)을 해칠 수 있음.
    // '@typescript-eslint/no-explicit-any': 'off' -> 이 규칙을 'off' 해서 any 타입을 사용해도 에러/경고를 띄우지 않음. (처음에는 편리하지만, 나중에는 'warn'이나 'error'로 바꿔서 엄격하게 관리하는 것이 좋음)
    '@typescript-eslint/no-explicit-any': 'off',

    // TypeScript 사용 시, 함수가 '어떤 종류의 데이터'를 반환할지 타입을 명시하는 것을 강제하는 규칙임.
    // '@typescript-eslint/explicit-module-boundary-types': 'off' -> 이 규칙을 'off' 해서 함수의 반환 타입을 명시하지 않아도 에러/경고를 띄우지 않음. (처음에는 편리하지만, 나중에는 켜는 것이 코드의 명확성을 높임)
    '@typescript-eslint/explicit-module-boundary-types': 'off',

    // Vue 컴포넌트의 이름은 '두 단어 이상'으로 짓는 것을 권장하는 규칙임.
    // 예: 'HeaderButton' (O), 'Btn' (X) -> 이름만 보고 어떤 컴포넌트인지 더 쉽게 알 수 있도록 하기 위함임.
    // [ 'warn', { ignores: [...] } ] -> 규칙 레벨은 'warn'(경고)로 하고,
    //                                   ignores 목록에 있는 이름들은 이 규칙 검사에서 '제외'함.
    // Ionic 기본 페이지 컴포넌트들(Home, Tabs 등)은 한 단어인 경우가 많아서 예외 처리를 해둔 것임.
    'vue/multi-word-component-names': [
      'warn', // 규칙 위반 시 경고를 띄움.
      {
        ignores: ['Home', 'Tabs', 'App', 'Tab1', 'Tab2', 'Tab3'], // 이 이름들은 규칙 검사에서 무시함. 필요에 따라 다른 한 단어 컴포넌트 이름을 추가할 수 있음.
      },
    ],
    // 필요에 따라 우리 프로젝트에서만 적용하고 싶은 '추가 규칙'들을 여기에 계속 정의할 수 있음.
    // 'custom-rule-name': 'error',
    // 'another-rule': ['warn', { ...옵션 }],
  },
};