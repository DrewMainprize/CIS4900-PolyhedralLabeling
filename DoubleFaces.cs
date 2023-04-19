using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class DoubleFaces : MonoBehaviour
{
    public GameObject model;
    // Start is called before the first frame update
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        
    }

    // Code taken from:
    // https://answers.unity.com/questions/280741/how-make-visible-the-back-face-of-a-mesh.html

    public void DoublicateFaces()
    {
        for (int i = 0; i < model.GetComponentsInChildren<Renderer>().Length; i++)
        {
            //Get original mesh components: vertices, normals, triangles and texture coordinates
            Mesh mesh = model.GetComponentsInChildren<MeshFilter>()[i].mesh;
            Vector3[] vertices = mesh.vertices;
            Vector3[] normals = mesh.normals;

            int numVertices = vertices.Length;
            int[] triangles = mesh.triangles;
            int numTriangles = triangles.Length;
            Vector2[] textureCoordinates = mesh.uv;

            // Checks if mesh does not have texture coordinates
            if (textureCoordinates.Length < numTriangles)
            {
                textureCoordinates = new Vector2[numVertices * 2];
            }

            // Creates a new mesh component, double the size of the original
            Vector3[] newVertices = new Vector3[numVertices * 2];
            Vector3[] newNormals = new Vector3[numVertices * 2];
            int[] newTriangle = new int[numTriangles * 2];
            Vector2[] newTextureCoordinates = new Vector2[numVertices * 2];

            for (int j = 0; j < numVertices; j++)
            {
                newVertices[j] = newVertices[j + numVertices] = vertices[j]; // Copy original vertices to make the second half of the new array
                newTextureCoordinates[j] = newTextureCoordinates[j + numVertices] = textureCoordinates[j]; // Copy original texture coordinates
                newNormals[j] = normals[j]; // First half of the new normals array is a copy of the original normals
                newNormals[j + numVertices] = -normals[j]; // Second half of the new normals array is the reverse of the original normals
            }

            for (int x = 0; x < numTriangles; x += 3)
            {
                // Copy the original triangle for the first half of the array
                newTriangle[x] = triangles[x];
                newTriangle[x + 1] = triangles[x + 1];
                newTriangle[x + 2] = triangles[x + 2];

                // Reverse triangles for second half of array
                int j = x + numTriangles;
                newTriangle[j] = triangles[x] + numVertices;
                newTriangle[j + 2] = triangles[x + 1] + numVertices;
                newTriangle[j + 1] = triangles[x + 2] + numVertices;
            }

            mesh.vertices = newVertices;
            mesh.uv = newTextureCoordinates;
            mesh.normals = newNormals;
            mesh.triangles = newTriangle;
        }
    }
}
