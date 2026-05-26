// Serve main compose resources during JS browser tests.
// The generated karma conf basePath is build/js/packages/<module>-test/, so
// we navigate up to the root and then into the library's processed resources.
const path = require('path');
const resourcesDir = path.resolve(__dirname, '../../../../library/build/processedResources/js/main');

config.files.push({
  pattern: resourcesDir + '/**',
  watched: false,
  included: false,
  served: true,
  nocache: false,
});

config.proxies['/composeResources/'] =
  '/absolute' + resourcesDir + '/composeResources/';
