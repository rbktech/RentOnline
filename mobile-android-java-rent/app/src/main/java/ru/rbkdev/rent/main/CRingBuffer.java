package ru.rbkdev.rent.main;

public class CRingBuffer {

    public static final int SIZE_RING_BUFFER = 4096;

    int m_mask = SIZE_RING_BUFFER - 1;

    int m_read = 0;
    int m_write = 0;

    byte[] m_array = new byte[SIZE_RING_BUFFER];

    void Write(byte[] data, int size) {

        for(int i = 0; i < size; i++)
            m_array[m_write++ & m_mask] = data[i];
    }

    byte[] Read() {

        int size = GetSize();
        byte[] array = new byte[size];
        // byte[] array = new byte[SIZE_RING_BUFFER];

        int count = m_read;

        for(int i = 0; i < size; i++)
            array[i] = m_array[count++ & m_mask];

        return array;
    }

    int GetSize() {

        int read = m_read & m_mask;
        int write = m_write & m_mask;

        if(read > write)
            return SIZE_RING_BUFFER - read + write;
        else
            return write - read;
    }

    void Flush(int count) {

        m_read = (m_read + count) & m_mask;
    }
}
