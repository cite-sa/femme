import { SystemicMetadata } from './systemic-metadata';
import { Element } from './element';
import { Collection } from './collection';
import { Metadatum } from './metadatum';


export class DataElement extends Element {

	collections: Array<Collection>;
	dataElements: Array<Collection>;

	constructor() {
		super();
	}
}
