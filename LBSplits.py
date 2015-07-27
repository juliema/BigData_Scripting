#to run this, do
# python LBSplits.py <stdevs> <tree1> <tree2>...


import dendropy
import sys
import numpy as np

def analyze(t, stdevs = 1):
    t.encode_bipartitions()
    thresh = np.mean([i.length for i in t.edges() if i.length]) + np.std([i.length for i in t.edges() if i.length]) * stdevs
    print "mean is", np.mean([i.length for i in t.edges() if i.length])
    print "std is",   np.std([i.length for i in t.edges() if i.length])
    print "threshhold is", thresh
    l = []
    for e in t.edges(lambda e: e.length > thresh):
        s = e.bipartition.split_as_newick_string(t.taxon_namespace)
        s1, s2 = s.split("), (")
        if len(s1) < len(s2):
            l.append((e.length, s1))
        else:
            l.append((e.length, s2))
    l.sort()
    for le, sp in l:
        print le, ':', sp.replace(')','').replace('(', '')
        

if __name__ == "__main__":
    tl = dendropy.TreeList()
    stdevs = float(sys.argv[1])
    for i in sys.argv[2:]:
        tl.append(dendropy.Tree.get_from_path(i, 'newick'))
    for t in tl:
        analyze(t, stdevs)    
