module.exports = {
  whiteList: Array.from(
    new Set([
      'index.js',
      'src/navigators/index.tsx',
      'src/registry/index.ts',
      'src',
      'src/api',
      'src/components',
      'src/hooks',
      'src/utils',
      'app.json',
    ]),
  ),
  blackList: [],
};
