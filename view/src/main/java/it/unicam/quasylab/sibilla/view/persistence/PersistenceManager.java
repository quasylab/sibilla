/*******************************************************************************
 * MIT License


 * Copyright (c) 2020 Lorenzo Serini
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
/**
 * 
 */


package it.unicam.quasylab.sibilla.view.persistence;

import java.io.File;
import java.io.IOException;


/**
 * Classes that implement this interface are responsible for implementing a persistence manager.
 * 
 * @author LorenzoSerini
 *
 * @param <T>
 */
public interface PersistenceManager<T> {
	File DEFAULT_TXT_FILE = new File("view\\src\\main\\resources\\persistence\\JsonPersistence.txt");
	
	/**
	 * Saved l in file
	 * @param l
	 * @param file
	 * @throws IOException
	 */
	default void save (T l, String file)  throws IOException{
		save(l, new File(file));
	}
	
	/**
	 * Loads file
	 * @param file
	 * @return
	 * @throws IOException
	 */
	default T load(String file)  throws IOException{
		return load(new File(file));
	}
	
	/**
	 * Saves l in file
	 * @param l
	 * @param file
	 * @throws IOException
	 */
	void save(T l, File file) throws IOException;
	
	/**
	 * Loads T from file
	 * @param file
	 * @return T
	 * @throws IOException
	 */
	T load(File file) throws IOException;
	
	
}
