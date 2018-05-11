import { Metadatum } from '@app/models/metadatum';
import { SystemicMetadata } from '@app/models/systemic-metadata';

export interface DataElement {
	id: string;
	name: string;
	endpoint: string;
	metadata: Metadatum;
	systemicMetadata: SystemicMetadata;
}