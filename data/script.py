import json
import sys

WRITE_LIMIT = 25

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
            s = {"id":{"S":str(base+count)}, "type":{"S":"series"}, "name":{"S":"Season " + str(season)}, "episodes":{"L":episodes}}
            content.append(s)
            episodes = []
            seasons.append({"S":s["id"]["S"]})
        else:
            e = {"id":{"S":str(base+count)}, "type":{"S":"episode"}, "name":{"S":line}}
            content.append(e)
            episodes.append({"S":e["id"]["S"]})
        count += 1
    content.append({"id":{"S":str(base+count)}, "type":{"S":"franchise"}, "name":{"S":franchise_name}, "series":{"L":seasons}})

    requests = []
    for c in content:
        req = {"PutRequest":{"Item":c}}
        requests.append(req)

    i = 0
    while i * WRITE_LIMIT < len(requests):
        fname = prefix + str(i) + ".json"
        out = open(fname, "w")
        out.write(json.dumps({"Content":requests[i*25:min(len(requests),i*25+25)]}))
        i += 1

    print(str(len(requests)) + " items")
