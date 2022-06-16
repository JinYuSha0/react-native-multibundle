module.exports = {
  useGestureHandler: true,
  whiteList: Array.from(
    new Set([
      'index.js',
      'src/navigators/index.tsx',
      'src/registry/index.ts',
      // 'src/app.context.tsx',
      'src/bootstrap.tsx',
      'src/api',
      'src/components',
      'src/hooks',
      'src/utils',
      'app.json',
    ]),
  ),
  blackList: [],
  bootstrap: ['src/bootstrap.tsx'],
};
