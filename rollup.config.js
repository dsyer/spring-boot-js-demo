import resolve from 'rollup-plugin-node-resolve';

export default {
	input: 'src/main/js/index.js',
	output: {
	  file: 'target/classes/static/index.js',
	  format: 'esm'
	},
	plugins: [
		resolve({
			esm: true,
			main: true,
			browser: true
		  })
	]
  };