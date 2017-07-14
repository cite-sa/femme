import { Element } from './element';
import { Collection } from './collection';
import { Metadatum } from './metadatum';


export class DataElement extends Element {

	collection: Array<Collection>;

	constructor() {
		super();
	}
}
