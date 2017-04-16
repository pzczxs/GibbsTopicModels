# GibbsTopicModels
Gibbs sampling algorithms for several topic models, such as LDA, AT, and coAT. 

### 1.1. Description
Topic model is family of generative probabilistic models for discovering the main themes from a collection of documents. Examples of topic models include Latent Dirichlet Allocation (LDA) [1][2][3], Author-Topic (AT) model [4][5][6], and co-Author-Topic (coAT) model [7], and many others. 

The inference for topic models usually cannot be done exactly. A variety of approximate inference algorithms have appeared in recent years, such as mean-field variational methods, expectation propagation, and Monte Carlo Markov chain sampling (MCMC). In this toolbox, Gibbs sampling, a special case of MCMC, is utilized, since it provides a simple method for obtaining parameter estimates under Dirichlet priors and allows combination of estimates from several local maxima of the posterior distribution. 

### 1.2. News, Comments, and Bug Reports.
We highly appreciate any suggestion, comment, and bug report.

#### 1.3. License
Code (c) 2011 Jacob Eisenstein
[Licensed under the Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

## 2. How to Use GibbsTopicModels
### 2.1. Data Format
Please refer to the sample in `data/nips`.

### 2.2. How to Use
*LDA: Please refer to `LDA.java` in the package `cn/edu/bjut/ui`.

*AT: Please refer to `AT.java` in the package  `cn/edu/bjut/ui`. 

*coAT: Please refer to `coAT.java` in the package `cn/edu/bjut/ui`

### 2.3. Additional Information
This tool is written by XU, Shuo from Beijing University of Technology. If you find this tool useful, please cite GibbsTopicModels as follows

Xin An, Shuo Xu, Yali Wen, and Mingxing Hu, 2014. [A Shared Interest Discovery Model for Coauthor Relationship in SNS](http://dx.doi.org/10.1155/2014/820715). International Journal of Distributed Sensor Networks, Vol. 2014, No. 820715, pp. 1-9. 

For any question, please contact XU, Shuo xushuo@bjut.edu.cn OR pzczxs@gmail.com.

## 3. References
[1] David M. Blei, Andrew Y. Ng, and Michael I. Jordan, 2003. Latent Dirichlet Allocation. Journal of Machine Learning Research, Vol. 3, No. Jan, pp. 993-1022.

[2] Thomas L. Griffiths and Mark Steyvers, 2004. Finding Scientific Topics. Proceedings of the National Academy of Sciences of the United States of America, Vol. 101, No. Suppl, pp. 5228-5235.

[3] Gregor Heinrich, 2009. Parameter Estimation for Text Analysis. Technical Report Version 2.9. vsonix GmbH and University of Leipzig. 

[4] Michal Rosen-Zvi, Thomas Griffiths, Mark Steyvers, and Padhraic Smyth, 2004. The Author-Topic Model for Authors and Documents. Proceedings of the 20th International Conference on Uncertainty in Artificial Intelligence. pp. 487-494.

[5] Mark Steyvers, Padhraic Smyth, and Thomas Griffiths, 2004. Probabilistic Author-Topic Models for Information Discovery. Proceedings of the 10th ACM SIGKDD International Conference on Knowledge Discovery and Data Mining. pp. 306-315. 

[6] Michal Rosen-Zvi, Chaitanya Chemudugunta, Thomas Griffiths, and Padhraic Smyth, and Mark Steyvers, 2010. Learning Author-Topic Models from Text Corpora. ACM Transactions on Information Systems, Vol. 28, No. 1, pp. 1-38. 

[7] Xin An, Shuo Xu, Yali Wen, and Mingxing Hu, 2014. [A Shared Interest Discovery Model for Coauthor Relationship in SNS](http://dx.doi.org/10.1155/2014/820715). International Journal of Distributed Sensor Networks, Vol. 2014, No. 820715, pp. 1-9. 

