// jumuniyo/frontend/.eslintrc.js 예시 (JavaScript 전용으로 수정)
module.exports = {
  root: true,
  env: {
    node: true,
    'vue/setup-compiler-macros': true,
  },
  extends: [
    'plugin:vue/vue3-essential',
    // '@vue/typescript/recommended', // TypeScript 사용 안 할 경우 제거 또는 주석 처리
    'eslint:recommended',
    '@ionic/eslint-config/recommended',
    'prettier',
  ],
  parserOptions: {
    ecmaVersion: 2020,
    // parser: '@typescript-eslint/parser', // TypeScript 사용 안 할 경우 제거 또는 주석 처리
  },
  rules: {
    'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'vue/no-deprecated-slot-attribute': 'off',
    // '@typescript-eslint/no-explicit-any': 'off', // TypeScript 규칙이므로 제거 또는 주석
    // '@typescript-eslint/explicit-module-boundary-types': 'off', // TypeScript 규칙이므로 제거 또는 주석
    'vue/multi-word-component-names': [
      'warn',
      {
        ignores: ['Home', 'Tabs', 'App', 'Tab1', 'Tab2', 'Tab3', 'SignUpPage'], // SignUpPage도 예외 추가
      },
    ],
  },
};