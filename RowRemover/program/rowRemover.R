#usage

#load libraries and set optins
options(echo=FALSE)
options(warn=-1) 
options(width=10000)


#all variables
inputDataFile<-NULL
#ex: inputDataFile<-"ds76_student_step_export.txt"
workingDir = NULL
programDir = NULL
#operation either remove or keep
operation<-"remove"
column.var.name<-"Problem Name"
remove.values<-NULL
case.sensitive<-NULL
remove.null<-NULL

# Read script parameters
args <- commandArgs(trailingOnly = TRUE)
# parse commandline args
i = 1
while (i <= length(args)) {
  if (args[i] == "-file0") {
    if (length(args) == i) {
      stop("input data file name must be specified")
    }
    inputDataFile = args[i+1]
    i = i+1
  } else if (args[i] == "-workingDir") {
    if (length(args) == i) {
      stop("workingDir name must be specified")
    }
    # This dir is the working dir for the component instantiation.
    workingDir = args[i+1]
    i = i+1
  } else if (args[i] == "-programDir") {
    if (length(args) == i) {
      stop("programDir name must be specified")
    }
    # This dir is the root dir of the component code.
    programDir = args[i+1]
    programDir = paste(programDir, "/program/", sep="")
    i = i+1
  } else if (args[i] == "-operation") {
    if (length(args) == i) {
      stop("operation must be specified")
    }
    operation = args[i+1]
    if (operation != "remove" & operation != "keep" ) {
      stop("operation must be remove or keep")
    }
    
    i = i+1
  } 
  else if (args[i] == "-valueColumn") {
    if (length(args) == i) {
      stop("valueColumn must be specified")
    }
    column.var.name = args[i+1]
    #replace all angle brackets, parenthses, space an ash with period
    column.var.name <- gsub("[ ()-]", ".", column.var.name)
    i = i+1
  } else if (args[i] == "-removeValues") {
    #split by comma
    remove.values = strsplit(args[i+1], "\\s*,\\s*")[[1]]
    if (length(remove.values) == 0) {
      stop("removeValues must be specified")
    }
    i = i+1
  } else if (args[i] == "-caseSensitive") {
    if (length(args) == i) {
      stop("caseSensitive must be specified")
    }
    case.sensitive = args[i+1]
    if (tolower(case.sensitive) != "yes" && tolower(case.sensitive) != "no") {
      stop("caseSensitive must be yes or no")
    }
    if (tolower(case.sensitive) == "yes") {
      case.sensitive = TRUE
    } else {
      case.sensitive = FALSE
    }
    
    i = i+1
  } else if (args[i] == "-removeNull") {
    if (length(args) == i) {
      stop("removeNull must be specified")
    }
    remove.null = args[i+1]
    if (tolower(remove.null) != "yes" && tolower(remove.null) != "no") {
      stop("removeNull must be yes or no")
    }
    if (tolower(remove.null) == "yes") {
      remove.null = TRUE
    } else {
      remove.null = FALSE
    }
    
    i = i+1
  } 
  i = i+1
}
# Load raw data
ds<-read.table(inputDataFile, sep="\t", header=TRUE, quote="\"",comment.char = "",blank.lines.skip=TRUE)

#make the column.var.name into character
#ex: ds$Problem.Name <- as.character(ds$Problem.Name)
cmdString = paste("ds$", column.var.name, " <- as.character(ds$", column.var.name, ")", sep="")
eval(parse(text=cmdString))

#if null is in remove.values, take it out
#hasNull<-FALSE
#null.ind<-grep('null', tolower(remove.values))
#if (length(null.ind)!=0) {
#  remove.values<-remove.values[-null.ind[1]]
#  hasNull<-TRUE
#}

if (operation == "remove") {
  if (length(remove.values) > 0) {
    if (case.sensitive) {
        #ex: ds<-ds[which(!(ds$Problem.Name %in% remove.values)),]
        cmdString = paste("ds<-ds[which(!(ds$", column.var.name, " %in% remove.values)),]", sep="")
        eval(parse(text=cmdString))
    } else {
        #ex: ds<-ds[which(!(tolower(ds$Problem.Name) %in% tolower(remove.values))),]
        cmdString = paste("ds<-ds[which(!(tolower(ds$", column.var.name, ") %in% tolower(remove.values))),]", sep="")
        eval(parse(text=cmdString))
    }
  } 
  if (remove.null) {
    cmdString = paste("ds<-ds[!is.na(ds$", column.var.name, ") & ds$", column.var.name, " != \"\",]", sep="")
    eval(parse(text=cmdString))
  }
} else {
  temp.ds<-NULL
  temp.ds.null<-NULL
  if (length(remove.values) > 0) {
    if (case.sensitive) {
      #ex: temp.ds<-ds[which(ds$Problem.Name %in% remove.values),]
      cmdString = paste("temp.ds<-ds[which(ds$", column.var.name, " %in% remove.values),]", sep="")
      eval(parse(text=cmdString))
    } else {
      #ex: temp.ds<-ds[which(tolower(ds$Problem.Name) %in% tolower(remove.values)),]
      cmdString = paste("temp.ds<-ds[which(tolower(ds$", column.var.name, ") %in% tolower(remove.values)),]", sep="")
      eval(parse(text=cmdString))
    }
  }
  if (remove.null){
    #ex: temp.ds.null<-ds[is.na(ds$Problem.Name) | ds$Problem.Name == "",]
    cmdString = paste("temp.ds.null<-ds[is.na(ds$", column.var.name, ") | ds$", column.var.name, "  == \"\",]", sep="")
    eval(parse(text=cmdString))
  }
  if (length(temp.ds) == 0 && length(temp.ds.null) == 0) {
    ds <- names(ds)
  } else if (length(temp.ds) != 0 && length(temp.ds.null) != 0) {
    ds<-rbind(temp.ds, temp.ds.null)
    rn<-rownames(ds)
    ds<-ds[order(as.numeric(rn)), ]
  } else if (length(temp.ds) != 0 && length(temp.ds.null) == 0) {
    ds<-temp.ds
  } else if (length(temp.ds) == 0 && length(temp.ds.null) != 0) {
    ds<-temp.ds.null
  }
}


outputFile <- paste(workingDir, "/modified_file.txt", sep="")
write.table(ds, file=outputFile, sep="\t", quote=FALSE, na="", col.names=TRUE, append=FALSE, row.names=FALSE)



