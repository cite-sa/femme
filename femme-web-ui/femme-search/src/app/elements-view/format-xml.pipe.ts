import { Pipe, PipeTransform } from '@angular/core';
import * as vkbeautify from 'vkbeautify';

@Pipe({ name: 'formatXml' })
export class FormatXmlPipe implements PipeTransform {
	transform(value: string): string {
		return vkbeautify.xml(value, 2).trim();
	}
}