module.exports = {
  dependency: {
    platforms: {
      android: {
        packageInstance:
          'new MultiBundle(getApplicationContext(), getResources().getString(R.string.MultiBundleDefaultModuleName), getResources().getString(R.string.MultiBundleServerHost))',
      },
    },
  },
};
