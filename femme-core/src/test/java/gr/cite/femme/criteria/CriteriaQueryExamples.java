package gr.cite.femme.criteria;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Test;
import org.mockito.Matchers;

import gr.cite.femme.core.DataElement;
import gr.cite.femme.core.Element;
import gr.cite.femme.core.Metadatum;
import gr.cite.femme.query.criteria.CriteriaQuery;
import gr.cite.femme.query.criteria.UnsupportedQueryOperationException;
import gr.cite.femme.query.criteria.Where;
import gr.cite.femme.query.criteria.WhereBuilder;

public class CriteriaQueryExamples {

	@Test
	public void queryDataElement() {

		DataElement dataElement1 = mock(DataElement.class);
		DataElement dataElement2 = mock(DataElement.class);

		Metadatum metadatum1 = mock(Metadatum.class);
		Metadatum metadatum2 = mock(Metadatum.class);

		CriteriaQuery<DataElement> query = simpleMock();

		try {
			List<DataElement> elements = query.whereBuilder().expression(metadatum1).or()/*.exists(metadatum2).and()*/
					.expression(query.<DataElement>expressionFactory().expression(metadatum1).or().expression(metadatum2)).and()
					.isChildOf(dataElement2).and().isParentOf(dataElement1).build().find();
		} catch (UnsupportedQueryOperationException e1) {
			fail();
		}

	}

	/*@Test*/
	public void queryMetadata() {
		DataElement e2 = mock(DataElement.class);

		Metadatum m1 = mock(Metadatum.class);
		Metadatum m2 = mock(Metadatum.class);

		CriteriaQuery<Metadatum> query = simpleMock();

		List<Metadatum> elements = query.whereBuilder().expression(m1).or()/*.exists(m2).and()*/
				.expression(query.<Metadatum>expressionFactory().expression(m1).or().expression(m2)).and().isChildOf(e2).build()
				.find();

	}

	/*@Test(expected = UnsupportedQueryOperationException.class)*/
	public void queryMetadataWithUnsupportedQueryOperationException() throws UnsupportedQueryOperationException {
		DataElement e1 = mock(DataElement.class);

		CriteriaQuery<Metadatum> query = mockWithException();

		// call of unsupported isParentOf on DataElementMetadatum
		List<Metadatum> elements = query.whereBuilder().isParentOf(e1).build().find();

	}

	/*@Test*/
	public void queryByIdTest() {
		CriteriaQuery<DataElement> query = mock(CriteriaQuery.class);

		Element e = query.find("id1");

	}

	private static CriteriaQuery<Metadatum> mockWithException() {
		CriteriaQuery<Metadatum> query = mock(CriteriaQuery.class);
		Where<Metadatum> where = mock(Where.class);
		WhereBuilder<Metadatum> whereBuilder = mock(WhereBuilder.class);

		when(query.whereBuilder()).thenReturn(where);
		when(whereBuilder.build()).thenReturn(query);

		try {
			when(where.<Metadatum> isParentOf(any())).thenThrow(
					new UnsupportedQueryOperationException(Metadatum.class + " doen't have any children"));

			when(where.<Element> isParentOf(any())).thenThrow(
					new UnsupportedQueryOperationException(Metadatum.class + " doen't have any children"));

		} catch (UnsupportedQueryOperationException e) {
		}

		return query;
	}

	private static <T> CriteriaQuery<T> simpleMock() {
		CriteriaQuery<T> query = mock(CriteriaQuery.class);
		Where<T> where = mock(Where.class);
		WhereBuilder<T> whereBuilder = mock(WhereBuilder.class);

		when(query.whereBuilder()).thenReturn(where);
		when(query.<T>expressionFactory()).thenReturn(where);

		when(whereBuilder.build()).thenReturn(query);
		when(whereBuilder.or()).thenReturn(where);
		when(whereBuilder.and()).thenReturn(where);

		when(where.<WhereBuilder<T>> expression(any())).thenReturn(whereBuilder);
		when(where.<Metadatum> expression(Matchers.<Metadatum> any())).thenReturn(whereBuilder);
		/*when(where.<Metadatum> exists(Matchers.<Metadatum> any())).thenReturn(whereBuilder);*/
		try {
			when(where.<Metadatum> isParentOf(any())).thenReturn(whereBuilder);
			when(where.<Element> isParentOf(any())).thenReturn(whereBuilder);
		} catch (UnsupportedQueryOperationException e) {
		}
		when(where.<Element> isChildOf(Matchers.<Element> any())).thenReturn(whereBuilder);
		/*when(where.<Element> isChildOf(Matchers.<WhereBuilder<DataElement>> any()))
				.thenReturn(whereBuilder);*/

		return query;
	}

}
