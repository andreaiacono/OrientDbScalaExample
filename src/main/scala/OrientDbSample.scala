import com.orientechnologies.orient.core.metadata.schema.OType
import com.orientechnologies.orient.core.sql.OCommandSQL
import com.tinkerpop.blueprints.{Direction, Edge, Vertex}
import com.tinkerpop.blueprints.impls.orient._

import scala.collection.JavaConversions._

object OrientDbSample extends App {

    val WorkEdgeLabel = "Work"

    // opens the DB (if not existing, it will create it)
    val uri: String = "plocal:target/database/scala_sample"
    val graph: OrientGraph = new OrientGraph(uri)

    try {

        // if the database does not contain the classes we need (i.e. it was just created),
        // then adds them
        if (graph.getVertexType("Person") == null) {

            // we now extend the Vertex class for Person and Company
            val person: OrientVertexType = graph.createVertexType("Person")
            person.createProperty("firstName", OType.STRING)
            person.createProperty("lastName", OType.STRING)

            val company: OrientVertexType = graph.createVertexType("Company")
            company.createProperty("name", OType.STRING)
            company.createProperty("revenue", OType.LONG)

            // we now extend the Edge class for a "Work" relationship
            // between Person and Company
            val work: OrientEdgeType = graph.createEdgeType(WorkEdgeLabel)
            work.createProperty("startDate", OType.DATE)
            work.createProperty("endDate", OType.DATE)
        }
        else {

            // cleans up the DB since it was already created in a preceding run
            graph.command(new OCommandSQL("DELETE VERTEX V")).execute()
            graph.command(new OCommandSQL("DELETE EDGE E")).execute()
        }

        // adds some people
        // (we have to force a vararg call in addVertex() method to avoid ambiguous
        // reference compile error, which is pretty ugly)
        val johnDoe: Vertex = graph.addVertex("class:Person", Nil: _*)
        johnDoe.setProperty("firstName", "John")
        johnDoe.setProperty("lastName", "Doe")

        // we can also set properties directly in the constructor call
        val johnSmith: Vertex = graph.addVertex("class:Person", "firstName", "John", "lastName", "Smith")
        val janeDoe: Vertex = graph.addVertex("class:Person", "firstName", "Jane", "lastName", "Doe")

        // creates a Company
        val acme: Vertex = graph.addVertex("class:Company", "name", "ACME", "revenue", "10000000")

        // creates edge JohnDoe worked for ACME
        val johnDoeAcme: Edge = graph.addEdge(null, johnDoe, acme, WorkEdgeLabel)
        johnDoeAcme.setProperty("startDate", "2010-01-01")
        johnDoeAcme.setProperty("endDate", "2013-04-21")

        // another way to create an edge, starting from the source vertex
        val johnSmithAcme: Edge = johnSmith.addEdge(WorkEdgeLabel, acme)
        johnSmithAcme.setProperty("startDate", "2009-01-01")

        // prints all the people who works/worked for ACME
        val res: OrientDynaElementIterable = graph
          .command(new OCommandSQL(s"SELECT expand(in('${WorkEdgeLabel}')) FROM Company WHERE name='ACME'"))
          .execute()

        println("ACME people:")
        res.foreach(v => {

            // gets the person
            val person = v.asInstanceOf[OrientVertex]

            // checks if the out edge Work contains the "endDate" property
            val workEdgeIterator = person.getEdges(Direction.OUT, WorkEdgeLabel).iterator()
            val status = if (!workEdgeIterator.isEmpty && workEdgeIterator.next().getProperty("endDate") != null) "retired" else "active"

            println(s"Name: ${person.getProperty("lastName")}, ${person.getProperty("firstName")} [${status}]")
        })
    }
    finally {
        graph.shutdown()
    }
}