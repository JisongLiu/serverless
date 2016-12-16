import json
import sys
from random_words import RandomWords
import random

WRITE_LIMIT = 25
NUM_COMMENTS = 500
MIN_COMMENT_LENGTH = 1
MAX_COMMENT_LENGTH = 15

# cid: 0-53, 100-165, 
content_ids = [i for i in range(54)] + [i for i in range(100,166)] + [i for i in range(200,210)]
comments = []
emails = ["jens.vivre@gmail.com", "ln2334@columbia.edu", "David@gmail.com", "lz2467@columbia.edu", "garcia@blooper.com", "guest@gmail.com", "denzel@fordham.edu"]

if __name__ == "__main__":
    output = sys.argv[1]

    rw = RandomWords()
    for i in range(NUM_COMMENTS):
        length = random.randint(MIN_COMMENT_LENGTH, MAX_COMMENT_LENGTH)
        text = ' '.join([rw.random_word() for j in range(length)])
        user = random.choice(emails)
        cid  = random.choice(content_ids)
        comment = {"id":{"S":str(i)}, "user":{"S":user}, "content":{"S":str(cid)}, "comment":{"S":text}}
        comments.append(comment)

    requests = []
    for c in comments:
        req = {"PutRequest":{"Item":c}}
        requests.append(req)

    i = 0
    while i * WRITE_LIMIT < len(requests):
        fname = output + str(i) + ".json"
        out = open(fname, "w")
        out.write(json.dumps({"Comment":requests[i*25:min(len(requests),i*25+25)]}))
        i += 1

    print(str(len(requests)) + " items")
