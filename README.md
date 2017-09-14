# OpenStreetMap OSM Facts

This projects contains checks and proofs of few non documented facts.
Also, there are few tools to understand better the organization of PBFs files.

You can browse, read and download the result of every "fact" proof, but you can also folk the project and use it for your own.
To access to all this data I'm using angelcervera/osm4scala. At the moment, this library [does not support Spark out of the box](angelcervera/osm4scala#25), so I
extracted all OSMData Blobs using one the examples in the library and upload all to [my home made cluster]() with Hadoop. All processes are
going to use this data in Hadoop.

## Fact 1: Blocks overlapping
The pbfs osm format is based in blocks of data.
Every block of data contains a data of one type: Ways, Nodes, Relations, etc. To save spaces, all nodes are stored as a
delta from a lat/lon offset. Every block contains a different offset.

This first "Fact" is related with this way to save space. If nodes are stored as deltas from a commons offset per block,
then make sense group nearest point in the same block. This was my first assumption, so this fact is checking it.

As a result, I will proof that the assumption is wrong and nodes are not grouped by proximity.

In this first fact, I'm going to:
- Proof that my assumption is wrong. Points are not grouped per blocks.
- Generate a file with blobs bounding boxes.
- Implement a small visualization with leaflet and the previous file: [Check here for Faroe Islands](fact1/frontend/index.html)

In the ```BlobHeader```, there is a ```indexdata``` that can contain information about the following blob, as the bounding box that
contains all geo data. But this information is not standardized and is optional.
The same problem with the ```HeaderBBox``` that is optional in ```HeaderBlock```.
So to know the bounding box, it is necessary iterate over all records in the  ```OsmData``` object.

To execute the spark job that create the javascript array to use in the map:
```bash
  ./bin/spark-submit \
    --class com.acervera.osmfacts.fact1.Fact1Driver \
    --master yarn \
    --deploy-mode cluster \
    --num-executors 5 \
    --executor-cores 3 \
    --driver-java-options='-Dosm-facts.input=hdfs:///user/angelcervera/osm/blocks/planet -Dosm-facts.local-file-js-bounding=/home/angelcervera/planet/bboxes.js' \
    ~/fact1-assembly-0.1-SNAPSHOT.jar

```

## Fact 2: TODO: Nodes are unique.

Nodes are stored in separate blocks that streets, so nodes should be uniques.
In this "Fact" I will check that this is true. 


