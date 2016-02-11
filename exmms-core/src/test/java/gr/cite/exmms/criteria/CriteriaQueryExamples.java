package gr.cite.exmms.criteria;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Test;
import org.mockito.Matchers;

import gr.cite.exmms.core.DataElement;
import gr.cite.exmms.core.DataElementMetadatum;
import gr.cite.exmms.criteria.CriteriaQuery;
import gr.cite.exmms.criteria.UnsupportedQueryOperationException;
import gr.cite.exmms.criteria.Where;
import gr.cite.exmms.criteria.WhereBuilder;

public class CriteriaQueryExamples {

	@Test
	public void queryDataElement() {

		DataElement dataElement1 = mock(DataElement.class);
		DataElement dataElement2 = mock(DataElement.class);

		DataElementMetadatum metadatum1 = mock(DataElementMetadatum.class);
		DataElementMetadatum metadatum2 = mock(DataElementMetadatum.class);

		CriteriaQuery<DataElement> query = simpleMock();

		try {
			List<DataElement> elements = query.whereBuilder().expression(metadatum1).or().exists(metadatum2).and()
					.expression(query.expressionFactory().expression(metadatum1).or().expression(metadatum2)).and()
					.isChildOf(dataElement2).and().isParentOf(dataElement1).build().find();
		} catch (UnsupportedQueryOperationException e1) {
			fail();
		}

	}

	@Test
	public void queryMetadata() {
		DataElement e2 = mock(DataElement.class);

		DataElementMetadatum m1 = mock(DataElementMetadatum.class);
		DataElementMetadatum m2 = mock(DataElementMetadatum.class);

		CriteriaQuery<DataElementMetadatum> query = simpleMock();

		List<DataElementMetadatum> elements = query.whereBuilder().expression(m1).or().exists(m2).and()
				.expression(query.expressionFactory().expression(m1).or().expression(m2)).and().isChildOf(e2).build()
				.find();

	}

	@Test(expected = UnsupportedQueryOperationException.class)
	public void queryMetadataWithUnsupportedQueryOperationException() throws UnsupportedQueryOperationException {
		DataElement e1 = mock(DataElement.class);

		CriteriaQuery<DataElementMetadatum> query = mockWithException();

		// call of unsupported isParentOf on DataElementMetadatum
		List<DataElementMetadatum> elements = query.whereBuilder().isParentOf(e1).build().find();

	}

	@Test
	public void queryByIdTest() {
		CriteriaQuery<DataElement> query = mock(CriteriaQuery.class);

		DataElement e = query.find("id1");

	}

	private static CriteriaQuery<DataElementMetadatum> mockWithException() {
		CriteriaQuery<DataElementMetadatum> query = mock(CriteriaQuery.class);
		Where<DataElementMetadatum> where = mock(Where.class);
		WhereBuilder<DataElementMetadatum> whereBuilder = mock(WhereBuilder.class);

		when(query.whereBuilder()).thenReturn(where);
		when(whereBuilder.build()).thenReturn(query);

		try {
			when(where.<DataElementMetadatum> isParentOf(any())).thenThrow(
					new UnsupportedQueryOperationException(DataElementMetadatum.class + " doen't have any children"));

			when(where.<DataElement> isParentOf(any())).thenThrow(
					new UnsupportedQueryOperationException(DataElementMetadatum.class + " doen't have any children"));

		} catch (UnsupportedQueryOperationException e) {
		}

		return query;
	}

	private static <T> CriteriaQuery<T> simpleMock() {
		CriteriaQuery<T> query = mock(CriteriaQuery.class);
		Where<T> where = mock(Where.class);
		WhereBuilder<T> whereBuilder = mock(WhereBuilder.class);

		when(query.whereBuilder()).thenReturn(where);
		when(query.expressionFactory()).thenReturn(where);

		when(whereBuilder.build()).thenReturn(query);
		when(whereBuilder.or()).thenReturn(where);
		when(whereBuilder.and()).thenReturn(where);

		when(where.<WhereBuilder<T>> expression(any())).thenReturn(whereBuilder);
		when(where.<DataElementMetadatum> expression(Matchers.<DataElementMetadatum> any())).thenReturn(whereBuilder);
		when(where.<DataElementMetadatum> exists(Matchers.<DataElementMetadatum> any())).thenReturn(whereBuilder);
		try {
			when(where.<DataElementMetadatum> isParentOf(any())).thenReturn(whereBuilder);
			when(where.<DataElement> isParentOf(any())).thenReturn(whereBuilder);
		} catch (UnsupportedQueryOperationException e) {
		}
		when(where.<DataElement> isChildOf(Matchers.<DataElement> any())).thenReturn(whereBuilder);
		when(where.<DataElement> isChildOf(Matchers.<WhereBuilder<DataElement>> any()))
				.thenReturn(whereBuilder);

		return query;
	}

}
