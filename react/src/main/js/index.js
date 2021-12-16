import './style.scss';
import '@springio/utils/style.css';

import 'bootstrap';
import '@springio/utils';
import { Chart, BarController, BarElement, PieController, ArcElement, LinearScale, CategoryScale, Title, Legend } from 'chart.js';

Chart.register(BarController, BarElement, PieController, ArcElement, LinearScale, CategoryScale, Title, Legend);

import React from 'react';
import ReactDOM from 'react-dom';

class ChartChooser extends React.Component {
	constructor(props) {
		super(props);
		this.state = {};
		this.clear = this.clear.bind(this);
		this.bar = this.bar.bind(this);
		this.pie = this.pie.bind(this);
		this.doughnut = this.doughnut.bind(this);
	};
	pops(type, legend) {
		let chart = this;
		this.clear();
		fetch("/pops").then(response => {
			response.json().then(data => {
				data.type = type;
				data.options.plugins.legend.display = legend;
				chart.setState({ active: new Chart(document.getElementById("canvas"), data) });
			});
		});
	};
	clear() {
		if (this.state.active) {
			this.state.active.destroy();
		}
	};
	bar() {
		this.pops("bar", false);
	};
	pie() {
		this.pops("pie", true);
	};
	doughnut() {
		this.pops("doughnut", true);
	};
	render() {
		return <div>
			<button className="btn btn-primary" onClick={this.clear}>Clear</button>
			<button className="btn btn-primary" onClick={this.bar}>Bar</button>
			<button className="btn btn-primary" onClick={this.pie}>Pie</button>
			<button className="btn btn-primary" onClick={this.doughnut}>Doughnut</button>
		</div>;
	}
}
ReactDOM.render(
	<ChartChooser />,
	document.getElementById('chooser')
);

class Auth extends React.Component {
	constructor(props) {
		super(props);
		this.state = { user: 'Unauthenticated' };
	};
	componentDidMount() {
		let hello = this;
		fetch("/user").then(response => {
			response.json().then(data => {
				hello.setState({ user: `Logged in as: ${data.name}` });
			});
		});
	};
	render() {
		return <div id="auth">{this.state.user}</div>;
	}
};
class Hello extends React.Component {
	constructor(props) {
		super(props);
		this.state = { name: '', message: '' };
		this.greet = this.greet.bind(this);
		this.change = this.change.bind(this);
	};
	greet() {
		this.setState({ message: `Hello ${this.state.name}!` })
	}
	change(event) {
		this.setState({ name: event.target.value })
	}
	render() {
		return <div>
			<div id="greeting">{this.state.message}</div>
			<input id="name" name="value" type="text" value={this.state.name} onChange={this.change} />
			<button className="btn btn-primary" onClick={this.greet}>Greet</button>
		</div>;
	}
}
ReactDOM.render(
	<div className="container" id="hello">
		<Auth />
		<Hello />
	</div>,
	document.getElementById('hello')
);

class Content extends React.Component {
	constructor(props) {
		super(props);
		this.state = { html: '' };
		this.fetch = this.fetch.bind(this);
	};
	fetch() {
		let hello = this;
		fetch("/test").then(response => {
			response.text().then(data => {
				hello.setState({ html: data });
			});
		});
	}
	render() {
		return <div>
			<div dangerouslySetInnerHTML={{ __html: this.state.html }}></div>
			<button className="btn btn-primary" onClick={this.fetch}>Fetch</button>
		</div>;
	}
}
ReactDOM.render(
	<Content />,
	document.getElementById('root')
);

var events = new EventSource("/stream");
events.onmessage = e => {
	document.getElementById("load").innerHTML = e.data;
}
