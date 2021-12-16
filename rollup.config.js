import commonjs from '@rollup/plugin-commonjs';
import replace from '@rollup/plugin-replace';
import resolve from "@rollup/plugin-node-resolve";
import scss from "rollup-plugin-scss";
import postcss from "rollup-plugin-postcss";
import { babel } from '@rollup/plugin-babel';

export default {
  input: "src/main/js/index.js",
  output: {
    file: "target/classes/static/index.js",
    format: "esm",
  },
  plugins: [
    replace({
      'process.env.NODE_ENV': JSON.stringify('development'),
      'preventAssignment': true
    }),
    babel(),
    commonjs(),
    resolve({
      esm: true,
      main: true,
      browser: true,
    }),
    scss(),
    postcss(),
  ],
};