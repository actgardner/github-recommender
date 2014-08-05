# Convert the output files from Mahout into CSV for ingestion to MySQL

import json, os

lines = 0

with open('recommendations.csv', 'w') as output:
    for file in os.listdir("recommend"):
        print 'Opening {0}'.format(file)
        with open('recommend/{0}'.format(file)) as f:
            for line in f:
                lines +=1
                (k,v) = line.split('\t')
                v = v.replace("[","").replace("]","")
                print k
                for r in v.split(","):
                    (p,s) = r.split(':')
                    output.write('{0},{1},{2}\n'.format(k,p, s))

print '{0} users'.format(lines)
