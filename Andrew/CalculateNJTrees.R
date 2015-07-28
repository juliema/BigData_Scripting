setwd("C:\\Users\\AHD\\workspace\\HBVOut\\NJQCRd5")
require(ape)

calcDigits = function(x) {
  if(x<1) return(0)
  else return(1+calcDigits(x/10))
}

stem="Data.bp150_nt1000."
min = 1;
max = 150;
digits = calcDigits(max);
extrastem = "EXTRA."
maxE = 288;
digitsE = digits+1;
badtaxa = c()

for(i in min:max) {
  filename = paste(stem,formatC(i,width=digits,format="d",flag="0"),".fasta",sep="")
  dd = dist.dna(read.FASTA(filename),pairwise.deletion=TRUE)
  a = is.finite(rowSums(as.matrix(dd)))
  badrow = which(a %in% FALSE)
  if(length(badrow)==0) {
    print(paste("Computing tree for file ",i,sep=""))
    tree = bionj(dd)
    write.tree(tree,paste(stem,formatC(i,width=digits,format="d",flag="0"),".tre",sep=""))
    #print(paste("Tree written"))
  } else {
    print(paste("Could not compute tree: file ",i,sep=""))
    rows = which(is.finite(rowSums(as.matrix(dd))) %in% FALSE)
    #do the work to figure out which row/column is the "worst" one?
    if(length(rows)>1) {
      for(j in 1:length(rows)) {
        ind = which(is.finite(as.matrix(dd)[rows[j],]) %in% FALSE)
        if(length(ind) > 1)
          badtaxa = c(badtaxa, names(as.matrix(dd)[1,])[rows[j]])
      }
    }
    #print(badtaxa)
  }
}

for(i in 1:maxE) {
  filename = paste(stem,extrastem,formatC(i,width=digitsE,format="d",flag="0"),".fasta",sep="")
  dd = dist.dna(read.FASTA(filename),pairwise.deletion=TRUE)
  a = is.finite(rowSums(as.matrix(dd)))
  badrow = which(a %in% FALSE)
  if(length(badrow)==0) {
    print(paste("Computing tree for EXTRA file ",i,sep=""))
    tree = bionj(dd)
    write.tree(tree,paste(stem,extrastem,formatC(i,width=digitsE,format="d",flag="0"),".tre",sep=""))
    #print(paste("Tree written"))
  } else {
    print(paste("Could not compute tree: EXTRA file ",i,sep=""))
    rows = which(is.finite(rowSums(as.matrix(dd))) %in% FALSE)
    #do the work to figure out which row/column is the "worst" one?
    if(length(rows)>1) {
      for(j in 1:length(rows)) {
        ind = which(is.finite(as.matrix(dd)[rows[j],]) %in% FALSE)
        if(length(ind) > 1)
          badtaxa = c(badtaxa, names(as.matrix(dd)[1,])[rows[j]])
      }
    }
    #print(badtaxa)
  }
}

write(badtaxa,"RawPotentiallyBadSeqs.txt")
write(unique(badtaxa),"UniquePotentiallyBadSeqs.txt")
write.table(table(badtaxa),"TablePotentiallyBadSeqs.csv",sep=",")

