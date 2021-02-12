console.log('starting test server');
before(function(done) {
	app = require('../server');
	app.on('listening', function() {
		console.log('listening');
		done();
	});
	this.timeout(5000);
});
