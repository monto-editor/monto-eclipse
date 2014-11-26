library('plyr')
library('ggplot2')
library('outliers')
library('gridExtra')

args <- commandArgs(trailingOnly = TRUE)


df <- read.csv (
	args[1],
	col.names = c('event','class','method','source','version','time'),
	stringsAsFactors = FALSE)

removeCol <- function (c)
	function (df)
		df[,!(names(df) %in% c)]

renameCol <- function (c)
	function (df)
		rename(df,c)

compose <- function (f,g)
	function (x)
		f(g(x))

df1 <- df[!(df$class %in% c('MontoParseController','JsonPrettyPrinter')),]
start <- compose(renameCol(c('time' ='start')),removeCol('event')) (df1[df1$event == 'start',])
end <- compose(renameCol(c('time' ='end')),removeCol('event')) (df1[df1$event == 'end',])
df2 <- transform(merge(start,end),delta = end - start)

classes <- unique(df2$class)

plotClass <- function(df,class) {
	sub <- rm.outlier(df[df$class == class,]$delta)
	qplot(sub / 1e3) +
		xlab('latency (ms)') +
		ylab('count') +
		ggtitle(class) +
		geom_histogram()
}

plotClassMethod <- function(df,class,method) {
	df <- df[df$class == class,]
	sub <- rm.outlier(df[df$method == method,]$delta)
	qplot(sub / 1e3) +
		xlab('latency (ms)') +
		ylab('count') +
		ggtitle(paste(class,method)) +
		geom_histogram()
}

pdf(file=args[2])
grid.arrange(
	plotClass(df2,"JavaTokenizer"),
	plotClass(df2,"JavaParser"),
	plotClass(df2,"JavaOutliner"),
	plotClass(df2,"JavaCodeCompletion"),
	ncol=2)
dev.off()

pdf(file=paste("versionmsg-",args[2],sep=""))
grid.arrange(
	plotClassMethod(df2,"VersionMessage","encode"),
	plotClassMethod(df2,"VersionMessage","decode"),
	ncol=2)
dev.off()
