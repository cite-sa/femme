import { Pipe, PipeTransform } from '@angular/core';
import { FulltextSearchResult } from '@app/models/fulltext-search-result';

@Pipe({ name: 'extractFulltextFields' })
export class ExtractFulltextFieldsPipe implements PipeTransform {
	transform(value: FulltextSearchResult): string {
		let propertyNames: string[] = Object.getOwnPropertyNames(value.fulltext);
		let projectedFields =  propertyNames.filter(propertyName => (propertyName != "metadatumId") && (propertyName != "elementId"));
		return projectedFields.map(field => value.fulltext[field]).join(" - ");
	}
}