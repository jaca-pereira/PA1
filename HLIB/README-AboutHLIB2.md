/* Please keep this library only for research and experimental use
 * in NOVA LINCS Internal Research Activities.
 * Henrique Domingos, 17/Feb/2017
*/

HKIB/MLIB V.1.2r2
Author: Henrique Domingos
Rev. 17/Feb/2017
===============================
Notice:
This release dont include applications or complementary MW tools and
other runtime environments, namely serachable encryption algorithms to
support image-processing operations on multimodal encrypted data,
and applications in the Information Retrieval Research Domain,
according to my recent publications in:
- IEEE Transactions on Cloud Computing Journal (2017)
- DSN 2017 Intl. Conference on  Dependable Systems and Networks 

For those interested in this, please contact hj@fct.unl.pt.

hj@fct.unl, 17/Feb/2017
===============================

Installation Instructions

This version of the lib only uses (partially) native JCE-based
cryprographic providers, using by default the native providers
already included in the Java SDK framework. If you prefer,
you can simply use the cryptoproviders from BouncyCastle (BC), but
in this case you must install the BC crypto-providers and
iterate in the crypt code if you intend to use and instantiate
the correspondent algorithms.
If this is not the case, you must be able to install the library
using the default implementation.

Here are the steps to use the LIBS and to put ready for use.
This behavior is for use in a Eclipse IDE project.

I - Installation process

step 1) 
   Create a new eclipse Java Project naming it "crypt".
   Tested use for Java 8 as minimum requirement
   (but it should work with any recent version (I hope ;-))

step 2)
   Put the sources in the folder "crypt" to the sources folder of the project

step 3)
   You must refresh the project (File -> Refresh)

step 4)
   You must try to Run trial tests, or use the classes as a Library. 
   In Eclipse you will create your Project aside and include "crypt" 
   project in your java buildind path 
   (Properties -> Java Building Path -> Properties)

   You have small demo-programs to know how to use (quickly) the library
   See the Demos Section (below) on the available Demos you will find
   in the HLIB v1.2 r2 Distribution.
   

step 5)
   There are some initial demos on "how to use" the cryptographic primitives
   available from the library (see hlib/hj/mlib/*)
   These demos are in the hierarchy of sources (see hlib/hj/demos)
   You have equivalent demo-code in the root directory.

step 6)
   Try to write now more demos (ex., in a extended-demos diretory).
   As your first programming examples (demo1, demo2, demo3, ... etc) I
   suggest to write code to combine (or to compose) different methods,
   for example following an onion-based encryption model.
   Another suggestion is to think on cocrete application scenarios where
   you find that a composition of the provided primitives can be offeres
   as an upper-layer API to facilitate the effort of programmers that
   can be involved in developments for such application scenarios.



Demos:
=========
ALl demos are availabel as very simple programs using the Library
to demostrate how to use it to support diferent operations, using
encrypted data (encrypted with partially homomorphic cryptographic
constructions, schemed and algorithms):

EqualIv

TestEq:
Test if encypted data is equal to given input

TestMul:
Multiplication of two encrypted integers, resulting on
an encrypted product, in such a way the the decryption of the
product will be the product of the plaintext integers. This
test shows a multplicative homomorphic encryption use.

TestOpeInt:
Show that it is possible to obtain an ordered classification
operating encrypted values. This is based on an Order-Perserved
Encryption Scheme, implementing the Bodyreva Scheme and Algorithm.

TestSerach:
This demo shows a linear search (or exact match) on encrypted strings
(text)

Exp:
Demo to show that we can have exponentials of encrypted integers,
to compute an encrypted exponent (that when decrypted corrsponds
to the exponetiation of the involved plaintext integers)

TestSum:
Thsi demo show the use of a partial homomorphic encryption to
supporte encrypted addition operations, allowing the addition of
two encrypted integers to obtain the encrypted sum. When the sum is
decrypted it corresponds to the plaintext sum of the plaintext integers


