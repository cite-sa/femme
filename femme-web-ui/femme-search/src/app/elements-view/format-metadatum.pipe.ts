import { Pipe, PipeTransform } from '@angular/core';
import { Metadatum } from '@app/models/metadatum';
import * as vkbeautify from 'vkbeautify';

@Pipe({ name: 'formatMetadatum' })
export class FormatMetadatumPipe implements PipeTransform {
	transform(metadatum: Metadatum): string {
		if (metadatum.contentType.toLowerCase().includes("json")) {
			return JSON.stringify(JSON.parse(metadatum.value), null, " ");
		} else if (metadatum.contentType.toLowerCase().includes("xml")) {
			return vkbeautify.xml(metadatum.value, 2).trim();
		}
	}
}