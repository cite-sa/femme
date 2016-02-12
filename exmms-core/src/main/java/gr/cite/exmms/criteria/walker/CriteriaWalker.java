package gr.cite.exmms.criteria.walker;

import gr.cite.exmms.core.Collection;
import gr.cite.exmms.core.DataElement;
import gr.cite.exmms.core.Metadatum;
import gr.cite.exmms.criteria.CriteriaQuery;
import gr.cite.exmms.criteria.UnsupportedQueryOperationException;
import gr.cite.exmms.criteria.serializer.CriteriaQuerySerializer;
import gr.cite.exmms.criteria.serializer.PrintCriteriaQuery;

public class CriteriaWalker<T> {
	CriteriaQuerySerializer<T> querySerializer;

	CriteriaQuery<T> datastoreQuery;

	public CriteriaWalker(CriteriaQuerySerializer<T> querySerializer, CriteriaQuery<T> datastoreQuery) {
		super();
		this.querySerializer = querySerializer;
		this.datastoreQuery = datastoreQuery;
	}

	public void walk() throws UnsupportedQueryOperationException {
		WhereWalker.walk(querySerializer.getWhere(), datastoreQuery.whereBuilder(), datastoreQuery);
	}

	public static void main(String[] args) throws UnsupportedQueryOperationException {
		Metadatum m1 = new Metadatum();
		m1.setName("m1");
		Metadatum m2 = new Metadatum();
		m2.setName("m2");

		Metadatum m3 = new Metadatum();
		m3.setName("m3");
		Metadatum m4 = new Metadatum();
		m4.setName("m4");

		Metadatum m5 = new Metadatum();
		m5.setName("m5");

		DataElement d1 = new DataElement();
		d1.setId("d1");

		Collection c1 = new Collection();
		c1.setId("c1");

		CriteriaQuerySerializer<DataElement> expectedQuery = new CriteriaQuerySerializer<>();
		expectedQuery.whereBuilder().exists(m1).and().exists(m2).or()

				.expression(

						expectedQuery.<DataElement>expressionFactory().expression(m3).and().expression(m4))

				.or()

				.expression(

						expectedQuery.<DataElement>expressionFactory().expression(m3).and()

								.isChildOf(

										expectedQuery.<DataElement>expressionFactory().expression(m4))

								.or().isParentOf(m5))

				.and().isChildOf(c1).and().isParentOf(d1)

				.build();

		PrintCriteriaQuery<DataElement> datastoreQuery2 = new PrintCriteriaQuery<>();
		new CriteriaWalker<DataElement>(expectedQuery, datastoreQuery2).walk();
		System.out.println(datastoreQuery2.toString());
	}
	
	 /*
	  * exists m1 and exists m2 or 
	  * 	( m3 = null and m4 = null) 
	  * 	or 
	  * 	( m3 = null and 
	  * 		isChildOf( m4 = null) 
	  * 		or isParentOf m5) 
	  * 	and isChildOf c1 and isParentOf d1
	  */

}
