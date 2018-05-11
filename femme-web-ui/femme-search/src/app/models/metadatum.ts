import { SystemicMetadata } from "@app/models/systemic-metadata";

export interface Metadatum {
	id: string;
	elementId: string;
	endpoint: string;
	name: string;
	value: string;
	contentType: string;
	systemicMetadata: SystemicMetadata;
}