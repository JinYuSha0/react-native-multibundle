module.exports = {
  dependency: {
    platforms: {
      android: {
        packageInstance:
          'new MultiBundle(getApplicationContext(), getResources().getString(R.string.MultiBundleDefailtModuleName), getResources().getString(R.string.MultiBundleServerHost))',
      },
    },
  },
};
