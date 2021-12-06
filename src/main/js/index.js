import 'bootstrap';
import '@hotwired/turbo';
import '@springio/utils';
import { Application, Controller } from '@hotwired/stimulus';
import { Chart, BarController, BarElement, PieController, ArcElement, LinearScale, CategoryScale, Title, Legend } from 'chart.js';

Turbo.connectStreamSource(new EventSource("/stream"))
window.Stimulus = Application.start();

Chart.register(BarController, BarElement, PieController, ArcElement, LinearScale, CategoryScale, Title, Legend);

Stimulus.register("hello", class extends Controller {
	static targets = ["name", "output", "auth"]
	initialize() {
		let hello = this;
		fetch("/user").then(response => {
			response.json().then(data => {
				hello.authTarget.textContent = `Logged in as: ${data.name}`;
			});
		});
	}
	greet() {
		this.outputTarget.textContent = `Hello, ${this.nameTarget.value}!`;
	};
});

Stimulus.register("chart", class extends Controller {
	static targets = ["canvas"]
	pops(type, legend) {
		let chart = this;
		this.clear();
		fetch("/pops").then(response => {
			response.json().then(data => {
				data.type = type;
				data.options.plugins.legend.display = legend;
				chart.active = new Chart(chart.canvasTarget, data);
			});
		});;
	};
	clear() {
		if (this.active) {
			this.active.destroy();
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
});

