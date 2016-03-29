import numpy
import matplotlib.pyplot as plt

'''
binarySearchData = numpy.loadtxt("learningTest.txt", delimiter=",", skiprows=1)
binarySearchData = numpy.flipud(binarySearchData)
plt.plot(binarySearchData[:,-2])
'''

maxs = []
means = []
for i in range(0,11,1):
    fileName = "outFolder/learningTest" + "_" + str(float(i)/10).replace(".", "") + ".txt"
    binarySearchData = numpy.loadtxt(fileName, delimiter=",", skiprows=1)
    binarySearchData = numpy.flipud(binarySearchData)
    utilityData = binarySearchData[:,-2]
    maxs.append(max(utilityData))
    means.append(numpy.mean(utilityData))

plt.plot(maxs)
plt.plot(means)
plt.ylabel('advUtility')
plt.xlabel('threshold (x10)')
plt.show()
