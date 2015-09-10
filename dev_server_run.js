try {
require("source-map-support").install();
} catch(err) {
}
require("./target/backend.dev/goog/bootstrap/nodejs.js");
require("./target/backend.dev/server.js");
goog.require("netzwaechterlein.dev");
goog.require("cljs.nodejscli");
