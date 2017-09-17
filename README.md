# OpenStreetMap OSM Facts

This projects contains checks and proofs of few non documented facts.
Also, there are few tools to understand better the organization of PBFs files.

You can browse, read and download the result of every "fact" proof, but you can also folk the project and use it for your own.
To access to all this data I'm using angelcervera/osm4scala. At the moment, this library [does not support Spark out of the box](https://github.com/angelcervera/osm4scala/issues/25), so I
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
- Implement a small visualization with leaflet and the previous file: [Check here for Faroe Islands](https://angelcervera.github.io/osm-facts/)

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

## Fact 2: Unique IDs.

Every block contains unique types of entities. I supposed that Ids are uniques between entities.

So in this fact I will check:
- No Ids duplication between blocks (example: Same node  in different blocks )
- No same Id used by two different types of entities (example: Way and node with same Id )

To execute the spark job to count number of duplicates:
```bash
  ./bin/spark-submit \
    --class com.acervera.osmfacts.fact2.Fact2Driver \
    --master yarn \
    --deploy-mode cluster \
    --num-executors 5 \
    --executor-cores 3 \
    ~/fact2-assembly-0.1-SNAPSHOT.jar hdfs:///user/angelcervera/osm/blocks/planet
```

## Fact 3: No all connections between ways are at the ends of the way
To define a network, the best way is define vertices and edges. In the case of OSM, the equivalence could be ```edges = ways``` and ```vertices = nodes at the ends of the way```.

In this fact, we are going to proof that this is not the case in the osm files, because there are nodes used as connection between ways that are not at the ends.

```bash
  ./bin/spark-submit \
    --class com.acervera.osmfacts.fact3.Fact3Driver \
    --master yarn \
    --deploy-mode cluster \
    --num-executors 5 \
    --executor-cores 3 \
    ~/fact3-assembly-0.1-SNAPSHOT.jar hdfs:///user/angelcervera/osm/blocks/planet /home/angelcervera/planet/connections_not_at_the_ends
```

[In the demo](https://angelcervera.github.io/osm-facts/) you can see how there are four ways (**red**, **black**, **green** and **fuchsia**) that have vertices as the ends, but the **blue** one contains the intersection in the middle. 