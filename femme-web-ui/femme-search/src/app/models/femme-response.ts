import { FemmeResponseEntity } from "@app/models/femme-response-entity";

export interface FemmeResponse<T> {
	status: number,
	message: string,
	entity: FemmeResponseEntity<T>
}