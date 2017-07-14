import { SystemicMetadata } from './systemic-metadata';
import { Metadatum } from './metadatum';

export class Element {
	id: string;
	name: string;
	endpoint: string;
	metadata: Array<Metadatum>;
	systemicMetadata: SystemicMetadata;

	constructor() {

	}



}
