import { Element } from './element';
import { DataElement } from './data-element';
import { Metadatum } from './metadatum';


export class Collection extends Element {

	dataElements: Array<DataElement>;

	constructor() {
		super();
	}
}
