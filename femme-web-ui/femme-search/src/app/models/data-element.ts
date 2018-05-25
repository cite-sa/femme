import { Metadatum } from '@app/models/metadatum';
import { SystemicMetadata } from '@app/models/systemic-metadata';
import { Collection } from '@app/models/collection';

export interface DataElement {
	id: string;
	name: string;
	endpoint: string;
	metadata: Metadatum;
	systemicMetadata: SystemicMetadata;
	collections: Array<Collection>;
}