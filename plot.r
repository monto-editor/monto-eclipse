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

plotClass <- function(df,class) {
	sub <- rm.outlier(df[df$class == class,]$delta)
	qplot(sub / 1e6) +
		xlab('latency (ms)') +
		ylab('count') +
		ggtitle(class) +
		geom_histogram()
}

plotClassMethod <- function(df,class,method) {
	df <- df[df$class == class,]
	sub <- rm.outlier(df[df$method == method,]$delta)
	qplot(sub / 1e6) +
		xlab('latency (ms)') +
		ylab('count') +
		ggtitle(paste(class,method)) +
		geom_histogram()
}

plotMethod <- function(df,method) {
	sub <- rm.outlier(df[df$method == method,]$delta)
	qplot(sub / 1e6) +
		xlab('latency (ms)') +
		ylab('count') +
		ggtitle(paste(method)) +
		geom_histogram()
}

df1 <- df[!(df$class %in% c('JsonPrettyPrinter', 'MontoParseController')),]
start <- compose(renameCol(c('time' ='start')),removeCol('event')) (df1[df1$event == 'start',])
end <- compose(renameCol(c('time' ='end')),removeCol('event')) (df1[df1$event == 'end',])
df2 <- transform(merge(start,end),delta = end - start)

classes <- unique(df2$class)

pdf(file=paste("products-",args[2],sep=""))
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

# calculate round trip

df1 <- df[df$class == 'MontoParseController',]
start <- compose(renameCol(c('time' ='start')),removeCol('event')) (df1[df1$event == 'start',])
end <- compose(renameCol(c('time' ='end')),removeCol('event')) (df1[df1$event == 'end',])
df2 <- transform(merge(start,end,by=c('class','source','version')),delta = end - start)
df2 <- compose(renameCol(c('method.y'='method')), removeCol('method.x')) (df2)

pdf(file=paste("roundtrip-",args[2],sep=""))
grid.arrange(
	plotMethod(df2,"product_tokens"),
	plotMethod(df2,"product_outline"),
	plotMethod(df2,"product_completions"),
	ncol=2)
dev.off()
