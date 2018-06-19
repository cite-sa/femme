# FeMME
### Installation instructions
In order to install and run Java 8 and Tomcat 8 are needed.
Also, MongoDB and Elasticsearch must be installed.
FeMME consists of three main services, femme, femme-fulltext and femme-geo.

All aforementioned components are packaged as WAR files.
These files must be deployed in Tomcat.
Each service is configured by an accompanying configuration file.

#### femme
    #MongoDB host
    gr.cite.femme.datastore.db.host=localhost
    #MongoDB port
    gr.cite.femme.datastore.db.port=27017
    #FeMME DB name
    gr.cite.femme.datastore.db.name=femme-db
    
    #MongoDB host
    gr.cite.femme.datastore.metadata.db.host=localhost
    #MongoDB port
    gr.cite.femme.datastore.metadata.db.port=27017
    #FeMME metadata DB name
    gr.cite.femme.datastore.metadata.db.name=femme-db
    #FeMME GridFS bucket name
    gr.cite.femme.datastore.metadata.db.bucket.name=metadataGridFS
    
    #Elasticsearch host
    gr.cite.femme.datastore.metadata.index.db.host=es-devel1.local.cite.gr
    #Elasticsearch port
    gr.cite.femme.datastore.metadata.index.db.port=9200
    #Elasticsearch metadata index name
    gr.cite.femme.datastore.metadata.index.db.name=metadataindex
    
    #MongoDB host  
    gr.cite.femme.datastore.metadata.schema.index.db.host=localhost  
    #MongoDB port  
    gr.cite.femme.datastore.metadata.schema.index.db.port=27017  
    #FeMME metadata schema DB name  
    gr.cite.femme.datastore.metadata.schema.index.db.name=metadata-schema-db  
    
    #FeMME full-text service endpoint  
    gr.cite.femme.fulltext.endpoint=http://localhost:8080/femme-fulltext  

#### femme-fulltext
    #Elasticsearch host  
    gr.cite.femme.fulltext.index.host=es-devel1.local.cite.gr  
    #Elasticsearch port  
    gr.cite.femme.fulltext.index.port=9200  
    #Elasticsearch full-text index name  
    gr.cite.femme.fulltext.index.name=fulltext_search  
    
    #Elasticsearch host  
    gr.cite.femme.fulltext.semantic.index.taxonomies.host=es-devel1.local.cite.gr  
    #Elasticsearch port  
    gr.cite.femme.fulltext.semantic.index.taxonomies.port=9200  
    #Elasticsearch semantic search index name  
    gr.cite.femme.fulltext.semantic.index.taxonomies.name=semantic_search  
    gr.cite.femme.fulltext.semantic.index.taxonomies.type=taxonomies  

#### femme-geo
    #MongoDB host  
    gr.cite.femme.geo.mongodb.host=localhost  
    #MongoDB port  
    gr.cite.femme.geo.mongodb.port=27017  
    #FeMME geo DB name  
    gr.cite.femme.geo.mongodb.name=femme-geo-db  
    
    #FeMME service endpoint  
    gr.cite.femme.client.femme.url=http://localhost:8080/femme  
