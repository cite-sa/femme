import { SystemicMetadata } from './systemic-metadata';
export class Metadatum {
	id: string;
	elementId: string;
	name: string;
	endpoint: string;
	value: string;
	contentType: string;
	systemicMetadata: SystemicMetadata;

	constructor() { }
}
