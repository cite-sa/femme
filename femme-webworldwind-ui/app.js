require.config({
	baseUrl: 'libs/ace',
	paths: {
		jquery: '../jquery-3.1.1',
		ace: 'ace'
	}
});

requirejs(["./ext/language_tools"]);
requirejs(["./snippets/text"]);
requirejs(['../../app/main']);