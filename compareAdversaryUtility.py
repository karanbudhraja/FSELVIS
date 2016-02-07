import numpy
import matplotlib.pyplot as plt

binarySearchData = numpy.loadtxt("fullCycle_BinarySearch.txt", delimiter=",", skiprows=1)
plt.plot(binarySearchData[:,-2])
learningWithBinarySearchData = numpy.loadtxt("fullCycle_LearningWithBinarySearch.txt", delimiter=",", skiprows=1)
plt.plot(learningWithBinarySearchData[:,-2])
plt.show()
