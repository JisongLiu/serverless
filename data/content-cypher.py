import json
import sys

if __name__ == "__main__":
    prefix = sys.argv[1]
    f = open(prefix + ".txt")
    base = int(sys.argv[2]) # starting id
    franchise_name = sys.argv[3]

    count = 0
    season = 0
    episodes = []
    seasons = []
    content = []
    for line in f:
        line = line.strip()
        if len(line) == 0:
            continue
        if line == "$":
            season += 1
            s = {"id":str(base+count), "type":"series", "name":"Season " + str(season), "episodes":episodes}
            content.append(s)
            episodes = []
            seasons.append(s["id"])
        else:
            e = {"id":str(base+count), "type":"episode", "name":line}
            content.append(e)
            episodes.append(e["id"])
        count += 1
    content.append({"id":str(base+count), "type":"franchise", "name":franchise_name, "series":seasons})

    fname = prefix + ".cypher"
    out = open(fname, "w")
    out.write("CREATE ")
    content_strings = []
    for c in content:
        cstr = "{ id: \'%s\', name: \'%s\', type: \'%s\' }" % (c["id"], c["name"], c["type"])
        content_strings.append("(c{0}:Content {1})".format(c["id"], cstr))
    out.write(", ".join(content_strings))

    out.write(", ")
    relationship_strings = []
    for c in content:
        if c["type"] == "series":
            for eid in c["episodes"]:
                relationship_strings.append("(c%s)-[:episode_belongs_to]->(c%s)" % (eid, c["id"]))
        if c["type"] == "franchise":
            for sid in c["series"]:
                relationship_strings.append("(c%s)-[:season_belongs_to]->(c%s)" % (sid, c["id"]))
    out.write(", ".join(relationship_strings))

    print(str(len(content)) + " items")
